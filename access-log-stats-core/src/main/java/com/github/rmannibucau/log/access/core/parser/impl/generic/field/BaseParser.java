package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

/**
 * Basic segment parser using "glouton" algorithm to eat all characters it can
 * until some stop charater or the end of the buffer based on a whitelist predicate for valid characters.
 */
public abstract class BaseParser implements DataParser {
    protected abstract boolean isValidCharacter(char value);

    protected boolean isStopCharacter(final char value) {
        return Character.isWhitespace(value);
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        for (int i = offset; i < input.length; i++) {
            final char current = input[i];
            if (isStopCharacter(current)) {
                if (i == offset) { // empty values are indeed not valid (means the parser is not required)
                    return ParsedData.failed();
                }
                return ParsedData.of(new String(input, offset, i - offset), i - 1);
            }
            if (!isValidCharacter(current)) {
                return ParsedData.failed();
            }
        }
        final int count = input.length - offset;
        if (count == 0) {
            return ParsedData.failed();
        }
        return ParsedData.of(new String(input, offset, count), input.length - 1);
    }
}
