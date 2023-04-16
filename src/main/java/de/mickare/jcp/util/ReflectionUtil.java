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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }


    public static Stream<Field> getAllDeclaredFields(Class<?> cls, Class<? extends Annotation> annotationClass) {
        var superclass = cls.getSuperclass();
        if (superclass == null) {
            return Stream.empty();
        }
        return Stream.concat(
                getAllDeclaredFields(superclass, annotationClass),
                Arrays.stream(cls.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotationClass))
        );
    }

    public static <E extends Enum<E>> Class<E> checkIsEnum(Class<?> enumClass) {
        if (!enumClass.isEnum()) throw new RuntimeException("Class " + enumClass + " is not an Enum");
        //noinspection unchecked
        return (Class<E>) enumClass;
    }
}
