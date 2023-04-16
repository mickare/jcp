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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SubCommandsTest {

    @Test
    void testHelp() throws Exception {
        var builder = CommandPipeline.builder(TestCommand.class, "test");
        builder.addSubCommand(CommandSubA.class, "a");
        builder.addSubCommand(CommandSubB.class, "b");
        var pipeline = builder.build();

        var data = new Object();
        var context = pipeline.execute(data, new String[]{"a"});

        assertNotNull(context.getTraceIfPresent(CommandSubA.class));
        assertNull(context.getTraceIfPresent(CommandSubB.class));
    }

    @Usage(desc = "Test command description.")
    public static class TestCommand extends AbstractCommand<Object, CommandContext<Object>> {
        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }

    @Usage(desc = "Test command description.")
    public static class CommandSubA extends AbstractCommand<Object, CommandContext<Object>> {
        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }

    @Usage(desc = "Test command description.")
    public static class CommandSubB extends AbstractCommand<Object, CommandContext<Object>> {
        @Override
        public CommandContext<Object> execute(CommandContext<Object> context) throws Exception {
            return context;
        }
    }
}
