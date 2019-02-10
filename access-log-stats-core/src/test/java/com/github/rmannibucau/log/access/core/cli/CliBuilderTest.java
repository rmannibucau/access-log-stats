package com.github.rmannibucau.log.access.core.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CliBuilderTest {
    @Test
    void simpleBinding() {
        final CliBuilder cliBuilder = new CliBuilder(System.err)
                .option("path", "whatever", true)
                .option("active", "whatever", true);
        final Options options = cliBuilder.bind(Options.class, new String[]{
                "--path", "/tmp/access.log",
                "--active"
        }).orElseThrow();
        assertEquals("/tmp/access.log", options.path);
        assertTrue(options.active);
    }

    @Test
    void negateBoolean() {
        final CliBuilder cliBuilder = new CliBuilder(System.err)
                .option("path", "whatever", true)
                .option("active", "whatever", true);
        final Options options = cliBuilder.bind(Options.class, new String[]{
                "--path", "/tmp/access.log",
                "--no-active"
        }).orElseThrow();
        assertEquals("/tmp/access.log", options.path);
        assertFalse(options.active);
    }

    @Test
    void required() {
        final CliBuilder cliBuilder = new CliBuilder(System.err)
                .option("path", "whatever", true)
                .option("active", "whatever", true);
        assertThrows(IllegalArgumentException.class, () -> cliBuilder.bind(Options.class, new String[]{
                "--path", "/tmp/access.log",
        }));
    }

    public static class Options {
        private String path;
        private boolean active;
    }
}
