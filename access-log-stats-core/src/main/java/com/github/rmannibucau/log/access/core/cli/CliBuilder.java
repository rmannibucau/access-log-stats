package com.github.rmannibucau.log.access.core.cli;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CliBuilder {
    private final PrintStream helpStream;
    private final Map<String, Option> options = new HashMap<>();

    public CliBuilder(final PrintStream helpStream) {
        this.helpStream = helpStream;
    }

    // todo: i81n
    public CliBuilder option(final String name, final String description, final boolean required) {
        if (options.put(name, new Option(name, description, required)) != null) {
            throw new IllegalArgumentException("Conflicting option: '" + name + "'");
        }
        return this;
    }

    public <T> Optional<T> bind(final Class<T> type, final String[] args) {
        if (args.length == 1 && "help".equals(args[0])) {
            printHelp();
            return Optional.empty();
        }
        try {
            final T instance = type.getConstructor().newInstance();
            final Collection<Option> set = new ArrayList<>(options.size());
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    switch (args[i].length()) {
                        case 2: { // short notation for 1 char options
                            final Option option = options.get(args[i].substring(1));
                            i = bindOption(args, instance, set, i, option);
                        }
                        default:
                            // support long/short notations and negation for booleans
                            final Option option;
                            if (args[i].startsWith("--no-")) {
                                option = options.get(args[i].substring("--no-".length()));
                            } else if (args[i].startsWith("--")) {
                                option = options.get(args[i].substring(2));
                            } else {
                                option = options.get(args[i].substring(1));
                            }
                            i = bindOption(args, (T) instance, set, i, option);
                    }
                }
            }
            final String errors = options.values().stream()
                    .filter(it -> it.required)
                    .filter(it -> !set.contains(it))
                    .map(it -> "Missing option '" + it.name + "'")
                    .collect(joining("\n"));
            if (errors.length() > 0) {
                printHelp();
                throw new IllegalArgumentException(errors);
            }
            return Optional.of(instance);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        }
    }

    private void printHelp() {
        helpStream.println("Help:");
        helpStream.println();
        helpStream.println("  Options:");
        helpStream.println();
        options.values().stream()
                .sorted(comparing((Option opt) -> !opt.required).thenComparing((Option opt) -> opt.name))
                .forEach(it -> helpStream.println("    " + it.name + (it.required ? "*" : "") + ": " + it.description));
        helpStream.println();
    }

    private <T> int bindOption(final String[] args, final T instance,
                               final Collection<Option> set, final int i,
                               final Option option) {
        if (option == null) {
            throw new IllegalArgumentException("Unknown option " + args[i]);
        }
        set.add(option);
        return i + bind(option, instance, args, i);
    }

    // this can be enhanced a lot but this is not a cli framework/library, just something simplifying our code
    private <T> int bind(final Option option, final T instance, final String[] args, final int i) {
        try {
            final Field field = instance.getClass().getDeclaredField(option.name);
            if (!field.canAccess(instance)) {
                field.setAccessible(true);
            }
            if (boolean.class == field.getType()) { // no value
                field.set(instance, !args[i].startsWith("--no-"));
                return 0;
            }
            if (args.length <= i + 1) {
                throw new IllegalArgumentException("Missing value for option " + args[i]);
            }
            if (int.class == field.getType()) {
                field.set(instance, Integer.parseInt(args[i + 1]));
                return 1;
            }
            if (long.class == field.getType()) {
                field.set(instance, Long.parseLong(args[i + 1]));
                return 1;
            }
            if (String.class == field.getType()) {
                field.set(instance, args[i +1]);
                return 1;
            }
            throw new IllegalArgumentException("Unsupported option type for " + field);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static class Option {
        private final String name;
        private final String description;
        private final boolean required;

        private Option(final String name, final String description, final boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }
    }
}
