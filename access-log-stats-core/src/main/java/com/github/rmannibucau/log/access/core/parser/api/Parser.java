package com.github.rmannibucau.log.access.core.parser.api;

/**
 * Line parser API.
 */
public interface Parser {
    /**
     * @param input data to parse.
     * @return the parsed data for the line.
     */
    Line parse(char[] input);
}
