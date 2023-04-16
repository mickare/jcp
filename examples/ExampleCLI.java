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
package de.mickare.jcp.examples;

import de.mickare.jcp.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class ExampleCLI {

    public static void main(String[] args) {
        Context ctx = new Context("nice");
        try {
            Integer exitCode = buildPipeline().execute(ctx, args);
            System.exit(exitCode);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex);
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandPipeline<MainCommand, Context, Integer> buildPipeline() {
        CommandPipeline.Builder<MainCommand, ExampleCLI.Context, Integer> builder = CommandPipeline.builder(MainCommand.class, "myapp");
        return builder.build();
    }

    public record Context(String text) {
    }

    public static class MainCommand extends AbstractHelpedCommand<Context, Integer> {

        @Option(names = {"-y", "--yes"}, store_true = true, desc = "Flag that stores true")
        private boolean yes = false;

        @Option(names = {"--day"}, desc = "Day of the week")
        private DayOfWeek day = DayOfWeek.MONDAY;

        @Argument(name = "other", symbol = "N", nargs = 0, desc = "Additional args")
        private List<String> other = new ArrayList<>();

        public Integer execute2(CommandContext<Context> context) throws Exception {
            System.out.printf("text=%s, yes=%s; day=%s; other=%s%n",
                    context.getData().text, this.yes, this.day, String.join(",", this.other));
            return 0;
        }
    }
}