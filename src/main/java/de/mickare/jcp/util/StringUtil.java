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
package de.mickare.jcp.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class StringUtil {
    private StringUtil() {
    }

    public static Optional<String> firstNotEmpty(String... args) {
        return Stream.of(args).filter(Predicate.not(String::isEmpty)).findFirst();
    }

    public static List<StringBuilder> splitBlocks(String text, int maxWidth, int minWordLength) {
        assert 1 < minWordLength;
        assert minWordLength < maxWidth;
        List<StringBuilder> lines = new ArrayList<>();
        Supplier<StringBuilder> newBuilder = () -> {
            var sb = new StringBuilder();
            lines.add(sb);
            return sb;
        };
        StringBuilder current = newBuilder.get();

        for (var word : text.split("\\s")) {
            int c = 0;
            do {
                int space = maxWidth - current.length() - 1;
                var chunk = word.substring(c, Math.min(word.length(), Math.max(space, minWordLength)));
                if (chunk.length() <= space) {
                    if (c == 0 && current.length() != 0) current.append(" ");
                    current.append(chunk);
                } else {
                    if (c != 0) current.append('-');
                    current = newBuilder.get().append(chunk);
                }
                c += chunk.length();
            } while (c < word.length());
        }
        return lines;
    }

    public static StringBuilder appendPadding(StringBuilder out, int left, int length, char padChar) {
        for (int i = left; i < length; ++i) out.append(padChar);
        return out;
    }

    public static StringBuilder appendIndent(StringBuilder out, String header, String text, int indent, int maxWidth, int spacing) {
        assert indent < maxWidth;
        assert spacing < indent;
        if (text.isEmpty()) {
            out.append(header, 0, Math.min(header.length(), maxWidth));
            return out;
        }
        var padding = " ".repeat(indent);
        if (header.length() <= indent - spacing) {
            out.append(header);
            appendPadding(out, header.length(), indent, ' ');
        } else {
            out.append(header, 0, Math.min(header.length(), maxWidth))
                    .append(System.lineSeparator()).append(padding);
        }

        var blocks = splitBlocks(text, maxWidth - indent, 3);
        for (int i = 0; i < blocks.size(); ++i) {
            if (i != 0) out.append(padding);
            out.append(blocks.get(i));
        }

        return out;
    }
}
