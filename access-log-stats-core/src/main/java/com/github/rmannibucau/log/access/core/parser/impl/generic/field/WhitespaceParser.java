package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

public class WhitespaceParser implements DataParser {
    @Override
    public String name() {
        return " ";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        if (Character.isWhitespace(input[offset])) {
            return ParsedData.of(null, offset);
        }
        return ParsedData.failed();
    }
}
