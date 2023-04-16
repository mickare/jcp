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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCompleter<V> implements ICompleter {

    public static Class<? extends Enum<?>> getEnumClass(Class<?> enumClass) {
        assert Enum.class.isAssignableFrom(enumClass);
        //noinspection unchecked
        return (Class<? extends Enum<?>>) enumClass;
    }

    public @Nullable List<String> getAllAvailablePossibilities(CommandContext<?> context, CommandPipeline.Parameter<?> param, String arg) {
        final var type = param.getField().getType();
        if (Enum.class.isAssignableFrom(type)) {
            var enumClass = getEnumClass(type);
            return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).map(String::toLowerCase).toList();
        }
        if (Boolean.class == type) {
            return List.of("true", "false");
        }
        if (Integer.class == type) {
            return List.of("0");
        }
        return null;
    }

    @Override
    public @Nullable List<String> complete(CommandContext<?> context, CommandPipeline.Parameter<?> param, String arg) {
        var result = getAllAvailablePossibilities(context, param, arg);
        if (result != null) {
            return result.stream().filter(p -> p.startsWith(arg)).collect(Collectors.toList());
        }
        return null;
    }
}
