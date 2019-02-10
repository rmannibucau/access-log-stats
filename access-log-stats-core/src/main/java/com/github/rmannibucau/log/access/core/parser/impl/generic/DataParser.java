package com.github.rmannibucau.log.access.core.parser.impl.generic;

/**
 * Represents a subparser handling a single field.
 */
public interface DataParser {
    /**
     * @return data name for this field.
     */
    String name();

    /**
     * @return is the line valid without that field.
     */
    boolean isRequired();

    /**
     * @param input the incoming buffer.
     * @param offset the offset to iterate at in the buffer.
     * @return the data extracted from this parsing.
     */
    ParsedData read(char[] input, int offset);
}
