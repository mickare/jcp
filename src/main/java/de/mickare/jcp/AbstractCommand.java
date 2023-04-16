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

public abstract class AbstractCommand<V, R> implements ICommand<V, R> {

    public abstract R execute(CommandContext<V> context) throws Exception;

    public R executeNext(CommandContext<V> context, CommandPipeline<?, V, R> currentPipeline, CommandPipeline<?, V, R> nextPipeline,
                         String label, ArgsTokenizer args) throws Exception {
        return nextPipeline.execute(context, label, args);
    }
}
