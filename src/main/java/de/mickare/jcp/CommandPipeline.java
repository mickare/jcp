/*
 * Copyright 2023 Michael Käser
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of either the Apache License, Version 2.0 or the MIT License.
 *
 * You may obtain a copy of the Apache License, Version 2.0 and the MIT License at
 * <http://www.apache.org/licenses/LICENSE-2.0> and
 * <https://opensource.org/licenses/MIT>, respectively.
 */
package de.mickare.jcp;

import de.mickare.jcp.util.IntHashMap;
import de.mickare.jcp.util.ReflectionUtil;
import de.mickare.jcp.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CommandPipeline<C extends ICommand<V, R>, V, R> {
    @Getter
    private final @NonNull ParserTable parserTable;
    @Getter
    private final @NonNull Class<C> commandClass;
    @Getter
    private final @NonNull String name;
    @Getter
    private final @NonNull List<OptionParameter> options;
    @Getter
    private final @NonNull List<PositionalParameter> positional;
    @Getter
    private final @NonNull Map<String, CommandPipeline<?, V, R>> pipelines;
    @Getter
    private @Nullable CommandPipeline<?, V, R> parent = null;

    public static <C extends ICommand<V, R>, V, R> Builder<C, V, R> builder(
            @NonNull Class<C> cls,
            @NonNull String name
    ) {
        return new Builder<>(cls, name, new ParserTable());
    }

    private C createCommandInstance() {
        try {
            return commandClass.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public R execute(V data, String[] args) throws Exception {
        return execute(data, new ArgsTokenizer(args));
    }

    public R execute(V data, String label, String[] args) throws Exception {
        return execute(data, label, new ArgsTokenizer(args));
    }

    public R execute(V data, ArgsTokenizer args) throws Exception {
        return execute(data, this.name, args);
    }

    public R execute(V data, String label, ArgsTokenizer args) throws Exception {
        CommandContext<V> context = new CommandContext<>(args, data);
        return execute(context, label, args);
    }

    public R execute(CommandContext<V> context, ArgsTokenizer args) throws Exception {
        return execute(context, this.name, args);
    }

    public R execute(CommandContext<V> context, String label, ArgsTokenizer args) throws Exception {
        var cmd = createCommandInstance();
        context.append(this, commandClass, cmd, label);

        // Parse options
        IntHashMap<OptionParameter> countOptions = new IntHashMap<>();
        while (args.hasNext()) {
            final var current = args.peek();
            if (!current.startsWith("-")) break;
            var found = options.stream()
                    .filter(o -> Arrays.asList(o.annotation.names()).contains(current))
                    .findFirst();

            if (found.isPresent()) {
                args.skip();
                var param = found.get();
                var fieldType = param.field.getType();
                var count = countOptions.incrementAndGet(param);

                if (param.isFlag()) {
                    assert Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType);
                    if (param.annotation.store_false()) {
                        this.parserTable.get(Boolean.class).write(param, cmd, Boolean.FALSE);
                    } else if (param.annotation.store_true()) {
                        this.parserTable.get(Boolean.class).write(param, cmd, Boolean.TRUE);
                    } else {
                        throw new UnsupportedOperationException("unexpected flag");
                    }
                } else {
                    if (count != 1 && !param.annotation.repeatable()) {
                        throw new IllegalArgumentException("Option " + current + " is not repeatable");
                    }
                    if (!args.hasNext()) {
                        throw new IllegalArgumentException("missing value for option " + current);
                    }
                    this.parserTable.get(fieldType).parseInto(param, cmd, args.next());
                }

                if (param.annotation.skipParsing()) {
                    return cmd.execute(context);
                }
            } else {
                throw new IllegalArgumentException("unknown flag: " + current);
            }
        }

        // Check if all required options are satisfied
        var missingRequiredOptions = options.stream()
                .filter(opt -> opt.annotation.required() && countOptions.get(opt) == 0)
                .collect(Collectors.toSet());
        if (!missingRequiredOptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required option(s): " +
                            missingRequiredOptions.stream()
                                    .map(opt -> opt.field.getName())
                                    .collect(Collectors.joining(", "))
            );
        }

        // Parse positional
        for (var pos : this.positional) {
            var parser = this.parserTable.get(pos.field.getType());

            try {
                if (pos.nargs() > 0) {
                    if (!args.hasNext(pos.nargs())) {
                        throw new IllegalArgumentException("Missing arguments for positional " + pos.annotation.name());
                    }
                    for (int i = 0; i < pos.nargs(); ++i) {
                        parser.parseInto(pos, cmd, args.next());
                    }
                } else {
                    int count = 0;
                    while (args.hasNext()) {
                        parser.parseInto(pos, cmd, args.next());
                        count += 1;
                    }
                    if (count < Math.abs(pos.nargs())) {
                        throw new IllegalArgumentException("Missing arguments for positional " + pos.annotation.name());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException("Missing arguments for positional " + pos.annotation.name(), ex);
            }
        }

        if (args.hasNext()) {
            if (this.pipelines.isEmpty()) {
                throw new IllegalArgumentException("Unexpected arguments: " + args.stream().collect(Collectors.joining(" ")));
            } else {
                var nextLabel = args.next();
                var nextPipeline = this.getSubcommand(nextLabel);
                if (nextPipeline == null) throw new IllegalArgumentException("Unexpected subcommand: " + nextLabel);
                return cmd.executeNext(context, this, nextPipeline, nextLabel, args);
            }
        }
        return cmd.execute(context);
    }

    public @Nullable List<String> complete(V data, String label, ArgsTokenizer args) throws Exception {
        CommandContext<V> context = new CommandContext<>(args, data);
        return complete(context, label, args);
    }

    public @Nullable List<String> complete(CommandContext<V> context, String label, ArgsTokenizer args) throws Exception {
        var cmd = createCommandInstance();
        context.append(this, commandClass, cmd, label);

        if (!args.hasNext()) {
            return null;
        }

        Set<String> results = new HashSet<>();

        // Parse options
        IntHashMap<OptionParameter> countOptions = new IntHashMap<>();
        while (args.hasNext()) {
            final var current = args.peek();
            if (!current.startsWith("-")) {
                options.stream()
                        .filter(opt -> opt.annotation.repeatable() || !countOptions.containsKey(opt))
                        .<String>mapMulti((opt, buf) -> opt.getNames().forEach(buf))
                        .forEach(results::add);
                break;
            }
            args.skip();
            var found = options.stream()
                    .filter(o -> Arrays.asList(o.annotation.names()).contains(current))
                    .findFirst();
            if (args.remaining() > 0 && found.isPresent()) {
                var param = found.get();
                var fieldType = param.field.getType();

                if (param.annotation.skipParsing()) {
                    continue;
                }

                if (param.isFlag()) {
                    continue;  // skip
                } else {
                    var arg = args.next();
                    if (args.remaining() == 0) {
                        return param.getCompleter().complete(context, param, arg);
                    }
                }
            } else {
                options.stream()
                        .filter(opt -> opt.annotation.repeatable() || !countOptions.containsKey(opt))
                        .<String>mapMulti((opt, buf) -> opt.getNames().forEach(buf))
                        .filter(opt -> opt.startsWith(current))
                        .forEach(results::add);
            }
        }

        // Check if all required options are satisfied
        var missingRequiredOptions = options.stream()
                .filter(opt -> opt.annotation.required() && countOptions.get(opt) == 0)
                .collect(Collectors.toSet());
        if (!missingRequiredOptions.isEmpty()) {
            return missingRequiredOptions.stream()
                    .<String>mapMulti((opt, buf) -> opt.getNames().forEach(buf))
                    .sorted().toList();
        }

        // Parse positional
        for (var pos : this.positional) {
            if (pos.nargs() > 0) {
                if (!args.hasNext(pos.nargs())) {
                    return pos.getCompleter().complete(context, pos, args.last());
                }
            } else {
                if (!args.hasNext(Math.abs(pos.nargs()))) {
                    return pos.getCompleter().complete(context, pos, args.last());
                }
                return pos.getCompleter().complete(context, pos, args.last());
            }
        }

        if (args.hasNext()) {
            if (this.pipelines.isEmpty()) {
                return null;
            } else {
                var nextLabel = args.next();
                var nextPipeline = this.getSubcommand(nextLabel);
                if (nextPipeline == null) {
                    return this.pipelines.keySet().stream()
                            .filter(sub -> sub.startsWith(nextLabel)).toList();
                }
                return nextPipeline.complete(context, nextLabel, args);
            }
        }

        return results.stream().sorted().collect(Collectors.toList());
    }

    public @Nullable CommandPipeline<?, V, R> getSubcommand(String name) {
        return this.pipelines.get(name);
    }

    public Stream<CommandPipeline<?, V, R>> getAllParents() {
        if (this.parent != null) {
            return Stream.concat(parent.getAllParents(), Stream.of(parent));
        }
        return Stream.empty();
    }

    public static class ParserTable {
        private final @NonNull Map<Class<?>, IParser<?>> custom = new HashMap<>();

        public <V> void register(Class<V> cls, IParser<V> parser) {
            custom.put(cls, parser);
        }

        public <V> void registerIfAbsent(Class<V> cls, IParser<V> parser) {
            custom.putIfAbsent(cls, parser);
        }

        public <E extends Enum<E>> void registerEnum(Class<E> enumClass) {
            register(enumClass, new DefaultParser.EnumParser<>(enumClass));
        }

        public <E extends Enum<E>> void registerEnumIfAbsent(Class<E> enumClass) {
            registerIfAbsent(enumClass, new DefaultParser.EnumParser<>(enumClass));
        }

        public <V> @Nullable IParser<V> getIfPresent(Class<V> cls) {
            var parser = custom.get(cls);
            if (parser == null) {
                parser = DefaultParser.STATIC_PARSER.get(cls);
            }
            //noinspection unchecked
            return (IParser<V>) parser;
        }

        public <V> @NonNull IParser<V> get(Class<V> cls) {
            var result = getIfPresent(cls);
            if (result == null) throw new RuntimeException("no value parser for type " + cls.toString());
            return result;
        }
    }

    @RequiredArgsConstructor
    public static class Builder<C extends ICommand<V, R>, V, R> {
        private final @NonNull Class<C> commandClass;
        private final @NonNull String name;
        @Getter
        private final @NonNull ParserTable parser;
        private final List<Builder<?, V, R>> subcommands = new ArrayList<>();

        public <S extends ICommand<V, R>> Builder<S, V, R> addSubCommand(@NonNull Class<S> cls, @NonNull String name) {
            assert subcommands.stream().noneMatch(sub -> sub.commandClass == cls);
            assert subcommands.stream().noneMatch(sub -> sub.name.equals(name));
            Builder<S, V, R> sub = new Builder<>(cls, name, parser);
            subcommands.add(sub);
            return sub;
        }

        public CommandPipeline<C, V, R> build() {

            var options = ReflectionUtil.getAllDeclaredFields(commandClass, Option.class)
                    .map(field -> new OptionParameter(field, parser))
                    .toList();
            var positional = ReflectionUtil.getAllDeclaredFields(commandClass, Argument.class)
                    .map(field -> new PositionalParameter(field, parser))
                    .toList();

            options.stream()
                    .filter(opt -> opt.field.getType().isEnum())
                    .map(opt -> opt.field.getType())
                    .map(ReflectionUtil::checkIsEnum)
                    .forEach(parser::registerEnumIfAbsent);
            positional.stream()
                    .filter(opt -> opt.field.getType().isEnum())
                    .map(opt -> opt.field.getType())
                    .map(ReflectionUtil::checkIsEnum)
                    .forEach(parser::registerEnumIfAbsent);


            boolean unlimited = false;
            for (var pos : positional) {
                if (unlimited) throw new RuntimeException("Invalid command! Unlimited 'nargs' argument must be last.");
                if (pos.nargs() <= 0) {
                    unlimited = true;
                    if (!subcommands.isEmpty()) {
                        throw new RuntimeException("Invalid command! Subcommands not supported when 'nargs' is unlimited.");
                    }
                }
            }

            Map<String, CommandPipeline<?, V, R>> subPipelines = subcommands.stream()
                    .map(Builder::build)
                    .collect(Collectors.toUnmodifiableMap(CommandPipeline::getName, Function.identity()));

            var pipeline = new CommandPipeline<>(
                    parser,
                    commandClass,
                    name,
                    Collections.unmodifiableList(options),
                    Collections.unmodifiableList(positional),
                    Collections.unmodifiableMap(subPipelines)
            );
            subPipelines.values().forEach(child -> child.parent = pipeline);
            return pipeline;
        }
    }

    public static abstract class Parameter<A extends Annotation> {
        @Getter
        protected final @NonNull Field field;
        @Getter
        protected final @NonNull A annotation;
        @Getter
        private final @NonNull ParserTable parserTable;

        public Parameter(@NonNull Field field, @NonNull Class<A> annotationClass, @NonNull ParserTable parserTable) {
            this.field = field;
            this.annotation = field.getDeclaredAnnotation(annotationClass);
            this.parserTable = parserTable;
            this.field.setAccessible(true);
        }

        public boolean isPrimitive() {
            return field.getType().isPrimitive();
        }

        public abstract int nargs();

        public <V> IParser<V> getParserOf(Class<V> cls) {
            return this.parserTable.get(cls);
        }

        public IParser<?> getParser() {
            return this.parserTable.get(this.field.getType());
        }

        public abstract String getName();

        public abstract @NonNull ICompleter getCompleter() throws ReflectiveOperationException;
    }

    public static class OptionParameter extends Parameter<Option> {
        @Getter
        protected final @NonNull List<String> names;

        public OptionParameter(@NonNull Field field, @NonNull ParserTable parser) {
            super(field, Option.class, parser);
            assert this.annotation.names().length > 0;
            this.names = List.of(this.annotation.names());

            assert !this.isFlag() || Boolean.class.isAssignableFrom(field.getType()) || boolean.class.isAssignableFrom(field.getType());
        }

        @Override
        public int nargs() {
            return -1;
        }

        @Override
        public String getName() {
            return annotation.names()[annotation.names().length - 1];
        }

        @Override
        public @NonNull ICompleter getCompleter() throws ReflectiveOperationException {
            return this.annotation.complete().getConstructor().newInstance();
        }

        public String getSymbol() {
            return StringUtil.firstNotEmpty(annotation.symbol())
                    .orElseGet(() -> this.field.getName().toUpperCase());
        }

        public boolean isFlag() {
            return annotation.store_false() || annotation.store_true();
        }
    }

    public static class PositionalParameter extends Parameter<Argument> {
        public PositionalParameter(@NonNull Field field, @NonNull ParserTable parser) {
            super(field, Argument.class, parser);
        }

        @Override
        public int nargs() {
            return this.annotation.nargs();
        }

        @Override
        public String getName() {
            return annotation.name();
        }

        public String getSymbol() {
            return StringUtil.firstNotEmpty(this.annotation.symbol(), this.annotation.name())
                    .orElseGet(() -> this.field.getName().toUpperCase());
        }

        @Override
        public @NonNull ICompleter getCompleter() throws ReflectiveOperationException {
            return this.annotation.complete().getConstructor().newInstance();
        }
    }

}
