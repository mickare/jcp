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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnumCommandTest {

    @Test
    void testEnums() throws Exception {
        var pipeline = CommandPipeline.builder(TestCommand.class, "test")
                .build();
        var data = new Object();

        var context = pipeline.execute(data, new ArgsTokenizer(new String[]{
                "-v", "first", "second", "third"
        }));
        assertNotNull(context);
        var trace = context.getTraceIfPresent(TestCommand.class);
        assertNotNull(trace);
        var command = trace.getCommand();
        assertEquals(MyEnum.FIRST, command.value);
        assertEquals(List.of(MyEnum.SECOND, MyEnum.THIRD), command.rest);

    }

    public enum MyEnum {
        FIRST,
        SECOND,
        THIRD
    }

    public static class TestCommand extends AbstractCommand<Object, CommandContext<Object>> {
        @Option(names = {"-v"})
        private MyEnum value;
        @Argument(name = "rest", nargs = 2)
        private List<MyEnum> rest;

        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }

}
