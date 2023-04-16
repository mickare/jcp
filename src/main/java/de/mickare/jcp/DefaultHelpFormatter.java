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

import de.mickare.jcp.util.StringUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultHelpFormatter implements IHelpFormatter<String> {

    public static final DefaultHelpFormatter INSTANCE = new DefaultHelpFormatter();


    @Getter
    private int maxWidth = 256;
    @Getter
    private int indent = 12;

    public static String formatPositional(CommandPipeline.PositionalParameter param) {
        var symbol = param.getSymbol();
        final var nargs = param.nargs();
        var out = new StringBuilder();
        if (nargs != 0) {
            out.append(IntStream.range(0, Math.abs(nargs)).mapToObj(i -> symbol).collect(Collectors.joining(" ")));
        }
        if (nargs <= 0) {
            if (nargs != 0) out.append(" ");
            out.append(String.format("[%s..]", symbol));
        }
        return out.toString();
    }

    public static Optional<String> getPipelineDescription(CommandPipeline<?, ?, ?> pipeline) {
        var usage = pipeline.getCommandClass().getAnnotation(Usage.class);
        if (usage != null) {
            if (!usage.desc().isEmpty()) return Optional.of(usage.desc());
        }
        return Optional.empty();
    }

    public void setMaxWidth(int maxWidth) {
        if (maxWidth < indent) throw new IllegalArgumentException("MaxWidth must be greater than indent.");
        this.maxWidth = maxWidth;
    }

    public void setIndent(int indent) {
        if (indent < 0) throw new IllegalArgumentException("Indent must be greater than zero.");
        if (indent >= maxWidth) throw new IllegalArgumentException("Indent must be smaller than maxWidth.");
        this.indent = indent;
    }

    @Override
    public String formatUsage(CommandPipeline<?, ?, ?> pipeline) {
        var out = new StringBuilder();
        out.append("Usage: ")
                .append(Stream.concat(
                        pipeline.getAllParents().map(CommandPipeline::getName),
                        Stream.of(pipeline.getName())
                ).collect(Collectors.joining(" ")));
        if (!pipeline.getOptions().isEmpty()) {
            out.append(" [options]");
        }
        if (!pipeline.getPositional().isEmpty()) {
            out.append(" ")
                    .append(pipeline.getPositional().stream().map(DefaultHelpFormatter::formatPositional)
                            .collect(Collectors.joining(" ")));
        }
        if (!pipeline.getPipelines().isEmpty()) {
            out.append(" ").append("{cmd}");
        }
        return out.toString();
    }

    @Override
    public String formatHelp(CommandPipeline<?, ?, ?> pipeline) {

        //    Usage: <name> <subcommand> [options] [ARG0] [ARG1...] N N [N...]
        //
        //    <desc>
        //
        //    Positional arguments:
        //      ARG0     Desc
        //      ARG1     asdf
        //      N        wasdafasf
        //    Options:
        //      -x       Text
        //      -long=value
        //               Some other desc
        //

        var builder = new StringBuilder();
        builder.append(formatUsage(pipeline));

        getPipelineDescription(pipeline).ifPresent(desc -> {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append(desc);
        });

        if (!pipeline.getPipelines().isEmpty()) {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append("Commands:");
            for (var sub : pipeline.getPipelines().values()) {
                builder.append(System.lineSeparator());
                StringUtil.appendIndent(builder, sub.getName(), getPipelineDescription(sub).orElse(""),
                        indent, maxWidth, 2);
            }
        }

        if (!pipeline.getPositional().isEmpty()) {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append("Positional arguments:");
            for (var param : pipeline.getPositional()) {
                builder.append(System.lineSeparator());
                StringUtil.appendIndent(builder, param.getSymbol(), param.annotation.desc(),
                        indent, maxWidth, 2);
            }
        }

        if (!pipeline.getOptions().isEmpty()) {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append("Options:");

            var sortedOptions = new ArrayList<>(pipeline.getOptions());
            sortedOptions.sort((a, b) -> a.getNames().get(0).compareToIgnoreCase(b.getNames().get(0)));

            for (var param : sortedOptions) {
                builder.append(System.lineSeparator());

                String header;
                if (param.isFlag()) {
                    header = String.join(", ", param.getNames());
                } else {
                    header = param.getNames().stream().map(name -> name + " " + param.getSymbol())
                            .collect(Collectors.joining(", "));
                }
                StringUtil.appendIndent(builder, header, param.annotation.desc(),
                        indent, maxWidth, 2);
            }
        }

        return builder.toString();
    }

}
