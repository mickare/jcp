# [JCP](https://github.com/mickare/jcp) - Java Command Pipeline

![Build and Publish](https://github.com/mickare/jcp/actions/workflows/main.yml/badge.svg) [![CodeQL](https://github.com/mickare/jcp/actions/workflows/codeql.yml/badge.svg)](https://github.com/mickare/jcp/actions/workflows/codeql.yml) [![License](https://img.shields.io/badge/license-MIT%2FApache--2.0-informational?style=flat-square)](COPYRIGHT.md)

A Java library for creating custom command line parser and command pipelines.

Do you need a **slim**, **minimalistic**, **easy to use** and **extensible** command line parser?
You want to return more than just an integer?
You want to supply custom context data to the command execution?
Then you may have found the right library.

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [FAQ](#faq)
- [Contributing](#contributing)
- [License](#license)


## Install

> Placeholders! The project has not yet been released to maven central

**Maven**

```
<dependency>
    <groupId>de.mickare.jcp</groupId>
    <artifactId>jcp</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Gradle (Groovy)**

```
dependencies {
    implementation 'de.mickare.jcp:jcp:0.0.1'
}
```

**Gradle (Kotlin)**

```
dependencies {
    implementation("de.mickare.jcp:jcp:0.0.1")
}
```

## Usage

Define your commands with a class and declare the command arguments and options as class fields.
Options and arguments must be annotated and are inherited from base classes.

```java
import de.mickare.jcp.*;

public class MyCommand extends AbstractCommand<V, R> {

    @Option(names = {"-v", "--value"})
    private final String value = false;

    @Argument(name = "other", symbol = "N", nargs = 1)
    private List<String> other;

    public R execute(CommandContext<V> context) throws Exception {
        return #...
    }
}
```

Build the command pipeline with your commands.
The command pipeline will parse the input arguments, instantiate your command objects and fill the options and
arguments.

```java
public static CommandPipeline<MyCommand, V, R> buildPipeline(){
        CommandPipeline.Builder<MyCommand, V, R> builder=CommandPipeline.builder(MyCommand.class,"main");
        // Add custom data parser
        builder.getParser().register(MyCustomType.class,new MyCustomTypeParser());

        // Add subcommands
        builder.addSubcommand(MySubCommandA.class,"a");
        builder.addSubcommand(MySubCommandB.class,"b");
        return builder.build();
        }
```

Run the command pipeline with provided arguments and handle possible exceptions.

```java
public static R example(V context,String[]args)throws Exception{
        try{
        return buildPipeline().execute(context,args);
        }catch(IllegalArgumentException ex){
        // handle
        System.err.println(ex.getMessage());
        return null;
        }
        }
```

###

### Command Line Interface

This example shows how you can use JCP in a classic CLI use-case.
You can find this example in [examples](examples/ExampleCLI.java).

```java
package myapp;


import de.mickare.jcp.*;

import java.time.DayOfWeek;
import java.util.List;

public class Main {

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
        CommandPipeline.Builder<MainCommand, Main.Context, Integer> builder = CommandPipeline.builder(MainCommand.class, "myapp");
        return builder.build();
    }

    public record Context(String text) {
    }

    public static class MainCommand extends AbstractHelpedCommand<Context, Integer> {

        @Option(names = {"-y", "--yes"}, store_true = true, desc = "Flag that stores true")
        private final boolean yes = false;

        @Option(names = {"--day"}, desc = "Day of the week")
        private final DayOfWeek day = DayOfWeek.MONDAY;

        @Argument(name = "other", symbol = "N", nargs = 0, desc = "Additional args")
        private List<String> other;

        public Integer execute2(CommandContext<Context> context) throws Exception {
            System.out.printf("text=%s, yes=%s; day=%s; other=%s%n",
                    context.getData().text, this.yes, this.day, String.join(",", this.other));
            return 0;
        }
    }
}
```

## FAQ

### Why yet another CLI library?

There are many great & advanced CLI libraries for Java, but many are too specialized to be the main entrypoint.
By design those libraries expect an exit code to be returned back to the operating system.
And because it is never expected that a CLI is called more than once, there is no way to pass custom context data to the
command execution.
This makes it near impossible to use in other use-cases, e.g. when your application keeps running and parses new
commands from stdin.

### Is this a CLI library?

Yes and no.
You can use this library to parse and run commands, but you'll have to implement basic plumbing to make this a working
CLI.

## Contributing

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as below, without any additional terms or conditions.

## License

Copyright 2023 Michael Käser

This project is licensed under either of

- [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) ([`LICENSE-APACHE`](LICENSE-APACHE))
- [MIT license](https://opensource.org/licenses/MIT) ([`LICENSE-MIT`](LICENSE-MIT))

at your option.
