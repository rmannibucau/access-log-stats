package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

public class DigitsParser implements DataParser {
    private final boolean required;
    private final String name;
    private final int maxLen;

    public DigitsParser(final boolean required, final String name, final int maxLen) {
        this.required = required;
        this.name = name;
        this.maxLen = maxLen;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        int count = 0;
        for (int i = offset; i < Math.min(input.length, offset + maxLen); i++) {
            if (!Character.isDigit(input[i])) {
                break;
            }
            count++;
        }
        if (count == 0) {
            return ParsedData.failed();
        }
        return ParsedData.of(new String(input, offset, count), offset + count - 1);
    }
}
