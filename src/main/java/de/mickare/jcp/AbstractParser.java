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

public abstract class AbstractParser<V> implements IParser<V> {

    public abstract V parse(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception;

    public void write(CommandPipeline.Parameter<?> param, Object obj, V value) throws Exception {
        param.field.set(obj, value);
    }

    public void parseInto(CommandPipeline.Parameter<?> param, Object obj, String arg) throws Exception {
        write(param, obj, parse(param, obj, arg));
    }

    public abstract @Nullable String getHelp(CommandPipeline.Parameter<?> param);
}
