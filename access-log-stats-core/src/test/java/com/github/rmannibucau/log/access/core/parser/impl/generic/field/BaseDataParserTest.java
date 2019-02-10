package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

abstract class BaseDataParserTest {
    void parse(final DataParser parser,
               final String input, final int offset, final boolean success, final String value, final int endIndex) {
        final ParsedData result = parser.read(input.toCharArray(), offset);
        assertEquals(success, result.isSuccess());
        assertEquals(endIndex, result.getEndIndex());
        if (success) {
            assertEquals(value, result.getData());
        }
    }
}
