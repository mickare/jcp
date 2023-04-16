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

import org.jetbrains.annotations.Nullable;

public interface IParser<V> {

    V parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception;

    void write(CommandPipeline.Parameter<?> param, Object obj, V value) throws Exception;

    void parseInto(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception;


    default @Nullable String getHelp(CommandPipeline.Parameter<?> param) {
        return null;
    }
}
