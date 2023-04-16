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

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultParser {
    public static final Map<Class<?>, IParser<?>> STATIC_PARSER;

    static {
        Map<Class<?>, IParser<?>> parser = new LinkedHashMap<>();
        add(parser, boolean.class, Boolean.class, new BooleanParser());
        add(parser, byte.class, Byte.class, new ByteParser());
        add(parser, char.class, Character.class, new CharParser());
        add(parser, double.class, Double.class, new DoubleParser());
        add(parser, float.class, Float.class, new FloatParser());
        add(parser, int.class, Integer.class, new IntParser());
        add(parser, long.class, Long.class, new LongParser());
        add(parser, short.class, Short.class, new ShortParser());
        add(parser, void.class, Void.class, new VoidParser());
        parser.put(String.class, new StringParser());
        parser.put(List.class, new ListParser());
        STATIC_PARSER = Collections.unmodifiableMap(parser);
    }

    private DefaultParser() {
    }

    private static void add(Map<Class<?>, IParser<?>> c, Class<?> a, Class<?> b, IParser<?> parser) {
        c.put(a, parser);
        c.put(b, parser);
    }

    // *****************************************************************
    // Primitive types

    public static class BooleanParser extends AbstractParser<Boolean> {
        @Override
        public Boolean parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Boolean.parseBoolean(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Boolean value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setBoolean(obj, value.booleanValue());
            else
                param.field.setBoolean(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "boolean: true or false";
        }
    }

    public static class ByteParser extends AbstractParser<Byte> {
        @Override
        public Byte parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Byte.parseByte(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Byte value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setByte(obj, value.byteValue());
            else
                param.field.setByte(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "byte: '\\u002B'";
        }
    }

    public static class CharParser extends AbstractParser<Character> {
        @Override
        public Character parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            assert arg.length() == 1;
            return arg.charAt(0);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Character value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setChar(obj, value.charValue());
            else
                param.field.setChar(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "character: 'a'";
        }
    }

    public static class DoubleParser extends AbstractParser<Double> {
        @Override
        public Double parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Double.parseDouble(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Double value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setDouble(obj, value.doubleValue());
            else
                param.field.setDouble(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "double: 0.0";
        }
    }

    public static class FloatParser extends AbstractParser<Float> {
        @Override
        public Float parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Float.parseFloat(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Float value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setFloat(obj, value.floatValue());
            else
                param.field.setFloat(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "float: 0.0";
        }
    }

    public static class IntParser extends AbstractParser<Integer> {
        @Override
        public Integer parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Integer.parseInt(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Integer value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setInt(obj, value.intValue());
            else
                param.field.setInt(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "integer: 0";
        }
    }

    public static class LongParser extends AbstractParser<Long> {
        @Override
        public Long parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Long.parseLong(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Long value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setLong(obj, value.longValue());
            else
                param.field.setLong(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "long number: 0";
        }
    }

    public static class ShortParser extends AbstractParser<Short> {
        @Override
        public Short parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return Short.parseShort(arg);
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Short value) throws Exception {
            if (param.isPrimitive())
                //noinspection UnnecessaryUnboxing
                param.field.setShort(obj, value.shortValue());
            else
                param.field.setShort(obj, value);
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "short number: 0";
        }
    }

    public static class VoidParser extends AbstractParser<Void> {
        @Override
        public Void parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            throw new UnsupportedOperationException("void unsupported");
        }

        @Override
        public void write(CommandPipeline.Parameter<?> param, Object obj, Void value) throws Exception {
            throw new UnsupportedOperationException("void unsupported");
        }

        @Override
        public void parseInto(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            throw new UnsupportedOperationException("void unsupported");
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "not supported";
        }
    }

    // *****************************************************************
    // Complex types

    public static class StringParser extends AbstractParser<String> {
        @Override
        public String parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            return arg;
        }

        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "any string";
        }
    }

    public static class ListParser extends AbstractParser<List<?>> {

        private static <V> List<V> getOrCreateList(CommandPipeline.Parameter<?> param, Object obj) throws Exception {
            var data = param.field.get(obj);
            if (data instanceof List) {
                //noinspection unchecked
                return (List<V>) data;
            } else {
                var list = new ArrayList<V>();
                param.field.set(obj, list);
                return list;
            }
        }

        private static <V> List<V> parseAndAppendList(CommandPipeline.Parameter<?> param, Object obj,
                                                      String arg, Class<? extends V> valueClass) throws Exception {
            var valueParser = param.getParserOf(valueClass);
            var value = valueParser.parse(param, obj, arg);
            List<V> list = getOrCreateList(param, obj);
            if (param.nargs() > 0) {
                if (list.size() >= param.nargs()) {
                    throw new IllegalArgumentException();
                }
            }
            list.add(value);
            return list;
        }

        private static IParser<?> getSubParser(CommandPipeline.Parameter<?> param) {
            assert param.field.getType().isAssignableFrom(ArrayList.class);
            var subtype = (ParameterizedType) param.field.getGenericType();
            Class<?> subclass = (Class<?>) subtype.getActualTypeArguments()[0];
            return param.getParserOf(subclass);
        }

        @Override
        public List<?> parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            assert param.field.getType().isAssignableFrom(ArrayList.class);
            var subtype = (ParameterizedType) param.field.getGenericType();
            Class<?> subclass = (Class<?>) subtype.getActualTypeArguments()[0];
            return parseAndAppendList(param, obj, arg, subclass);
        }

        @Nullable
        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return getSubParser(param).getHelp(param);
        }
    }

    @Getter
    public static class EnumParser<E extends Enum<E>> extends AbstractParser<E> {
        private final @NonNull Class<E> enumClass;
        private final @NonNull Map<String, E> values;

        public EnumParser(@NonNull Class<E> enumClass) {
            this.enumClass = enumClass;
            this.values = Arrays.stream(enumClass.getEnumConstants())
                    .collect(Collectors.toUnmodifiableMap(e -> e.name().toLowerCase(), Function.identity()));
        }

        @Override
        public E parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
            var result = values.get(arg.toLowerCase());
            if (result == null)
                throw new IllegalArgumentException(String.format("Expected enum %s but received \"%s\"", param.getName(), arg));
            return result;
        }

        @Nullable
        @Override
        public String getHelp(CommandPipeline.Parameter<?> param) {
            return "choice: " + String.join(", ", values.keySet());
        }
    }
}
