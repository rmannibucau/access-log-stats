package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

public class CharParser implements DataParser {
    private final boolean required;
    private final char value;

    public CharParser(final boolean required, final char value) {
        this.required = required;
        this.value = value;
    }

    @Override
    public String name() {
        return Character.toString(value);
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        if (input[offset] != value) {
            return ParsedData.failed();
        }
        return ParsedData.of(null, offset);
    }
}
