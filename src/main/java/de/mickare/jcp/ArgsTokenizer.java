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
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ArgsTokenizer {
    @Getter
    private final @NonNull String[] allArgs;
    @Getter
    private int index = 0;

    public String peek() {
        return allArgs[index];
    }

    public String[] peek(int size) {
        assert size >= 0;
        return Arrays.copyOfRange(allArgs, index, index + size);
    }

    public String next() {
        return allArgs[index++];
    }

    public String[] next(int size) {
        assert size >= 0;
        return Arrays.copyOfRange(allArgs, index, index += size);
    }

    public boolean hasNext() {
        return index < allArgs.length;
    }

    public boolean hasNext(int size) {
        return index + size <= allArgs.length;
    }

    public void skip() {
        index += 1;
    }

    public void skip(int size) {
        assert size > 0;
        index += size;
    }


    public int remaining() {
        return allArgs.length - index;
    }

    public int total() {
        return allArgs.length;
    }

    public Stream<String> stream() {
        var stream = Arrays.stream(allArgs, index, allArgs.length);
        index = allArgs.length;
        return stream;
    }

    public Stream<String> peekStream() {
        return Arrays.stream(allArgs, index, allArgs.length);
    }

    public void setIndex(int index) {
        assert index >= 0;
        this.index = index;
    }

    public String last() {
        this.index = allArgs.length - 1;
        return allArgs[allArgs.length - 1];
    }
}
