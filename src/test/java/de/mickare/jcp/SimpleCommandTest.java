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

import static org.junit.jupiter.api.Assertions.*;

public class SimpleCommandTest {

    @Test
    void testOptions() throws Exception {
        var pipeline = CommandPipeline.builder(TestOptionsCommand.class, "test")
                .build();
        var data = new Object();

        var context = pipeline.execute(data, new String[]{
                "-t",
                "-f",
                "-r", "0",
                "-s", "testing",
                "-r", "1",
                "-r", "2",
                "-r", "3"
        });
        assertNotNull(context);
        var trace = context.getTraceIfPresent(TestOptionsCommand.class);
        assertNotNull(trace);
        var command = trace.getCommand();
        assertTrue(command.t);
        assertFalse(command.f);
        assertEquals("testing", command.test);
        assertEquals(List.of(0, 1, 2, 3), command.repeated);

    }

    @Test
    void testPositional() throws Exception {
        var pipeline = CommandPipeline.builder(TestPositionalCommand.class, "test")
                .build();
        var data = new Object();

        var context = pipeline.execute(data, new String[]{
                "true",
                "false",
                "0.1", "0.2", "0.3",
                "0", "1", "2", "3", "4", "5"
        });
        assertNotNull(context);
        var trace = context.getTraceIfPresent(TestPositionalCommand.class);
        assertNotNull(trace);
        var command = trace.getCommand();
        assertTrue(command.first1);
        assertFalse(command.seocnd2);
        assertEquals(List.of(0.1f, 0.2f, 0.3f), command.vector);
        assertEquals(List.of(0, 1, 2, 3, 4, 5), command.repeated);

    }

    public static class TestOptionsCommand extends AbstractCommand<Object, CommandContext<Object>> {
        @Option(names = {"-t"}, store_true = true)
        private boolean t = false;
        @Option(names = {"-f"}, store_false = true)
        private boolean f = true;

        @Option(names = "-s")
        private String test;

        @Option(names = {"-r"}, repeatable = true)
        private List<Integer> repeated;

        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }

    public static class TestPositionalCommand extends AbstractCommand<Object, CommandContext<Object>> {
        @Argument(name = "first")
        private boolean first1 = false;
        @Argument(name = "second")
        private boolean seocnd2 = true;
        @Argument(name = "vector", nargs = 3)
        private List<Float> vector;
        @Argument(name = "repeated", symbol = "N", nargs = -1)
        private List<Integer> repeated;

        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }
}
