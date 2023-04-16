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

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@AllArgsConstructor
public class CommandContext<V> {
    private final @NonNull Map<Class<? extends ICommand<V, ?>>, Trace<?>> traces = new HashMap<>();
    @Getter
    private ArgsTokenizer arguments;
    @Getter
    @Setter
    private V data = null;

    public <C extends ICommand<V, ?>> @NonNull Trace<C> append(@NonNull Trace<C> trace) {
        var old = this.traces.putIfAbsent(trace.type, trace);
        if (old != null) {
            throw new RuntimeException("unexpected command trace already in context");
        }
        return trace;
    }

    public <C extends ICommand<V, ?>> @NonNull Trace<C> append(CommandPipeline<C, V, ?> pipeline, Class<C> type, C command, String label) {
        return append(new Trace<>(pipeline, type, command, label));
    }


    public @NonNull Map<Class<? extends ICommand<V, ?>>, Trace<?>> getTraces() {
        return Collections.unmodifiableMap(traces);
    }

    public <C extends ICommand<V, ?>> @Nullable Trace<C> getTraceIfPresent(Class<C> type) {
        //noinspection unchecked
        return (Trace<C>) traces.get(type);
    }

    public <C extends ICommand<V, ?>> @Nullable Trace<C> getTraceIfPresent(C command) {
        //noinspection unchecked
        return (Trace<C>) getTraceIfPresent(command.getClass());
    }

    public <C extends ICommand<V, ?>> CommandPipeline<C, V, ?> getPipeline(Class<C> type) {
        var trace = getTraceIfPresent(type);
        if (trace == null) return null;
        return trace.getPipeline();
    }

    public <C extends ICommand<V, ?>> CommandPipeline<C, V, ?> getPipeline(C command) {
        var trace = getTraceIfPresent(command);
        if (trace == null) return null;
        return trace.getPipeline();
    }

    @Data
    public class Trace<C extends ICommand<V, ?>> {
        private final @NonNull CommandPipeline<C, V, ?> pipeline;
        private final @NonNull Class<C> type;
        private final @NonNull C command;
        private final @NonNull String label;
    }
}
