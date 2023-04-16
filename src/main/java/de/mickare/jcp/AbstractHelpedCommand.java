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

public abstract class AbstractHelpedCommand<V, R> extends AbstractCommand<V, R> {

    @Option(
            names = {"-h", "--help"},
            desc = "Show this help message.",
            store_true = true,
            skipParsing = true
    )
    protected boolean help = false;

    @Override
    public @Nullable R execute(CommandContext<V> context) throws Exception {
        if (help) {
            showHelp(context);
            return null;
        }
        return execute2(context);
    }

    public abstract R execute2(CommandContext<V> context) throws Exception;

    public void showHelp(CommandContext<V> context) {
        System.err.println(DefaultHelpFormatter.INSTANCE.formatHelp(context.getPipeline(this)));
    }
}
