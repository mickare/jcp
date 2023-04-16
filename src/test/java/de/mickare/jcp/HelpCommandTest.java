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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HelpCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testHelp() throws Exception {
        var pipeline = CommandPipeline.builder(TestCommand.class, "test")
                .build();
        var data = new Object();

        var context = pipeline.execute(data, new String[]{
                "--help"
        });
        assertNull(context);
        assertEquals("""
                Usage: test [options] N
                                
                Test command description.
                                
                Positional arguments:
                N           Positional argument description.
                                
                Options:
                -f, --flag
                -h, --help  Show this help message.
                -v VALUE    Some description.
                """, errContent.toString());
        assertEquals("", outContent.toString());
    }

    @Usage(desc = "Test command description.")
    public static class TestCommand extends AbstractHelpedCommand<Object, CommandContext<Object>> {
        @Option(names = {"-f", "--flag"}, store_true = true)
        private boolean flag = false;
        @Option(names = {"-v"}, desc = "Some description.")
        private String value;
        @Argument(name = "wow", symbol = "N", desc = "Positional argument description.")
        private List<String> wow;

        @Override
        public CommandContext<Object> execute2(CommandContext<Object> context) throws Exception {
            return context;
        }
    }

}
