package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import static java.util.Objects.requireNonNull;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

/**
 * Generic data parser iterating over a buffer using subparsers.
 */
public abstract class GenericDataParser implements DataParser {
    private final DataParser[] fields;

    protected GenericDataParser(final DataParser[] fields) {
        this.fields = requireNonNull(fields, "You can't parse logs without data");
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        int currentIndex = offset;
        for (final DataParser field : fields) {
            if (currentIndex >= input.length) {
                if (field.isRequired()) {
                    return ParsedData.failed();
                }
                continue; // else ok, let's check next field
            }

            final ParsedData parsedData = field.read(input, currentIndex);
            if (!parsedData.isSuccess() && field.isRequired()) {
                return ParsedData.failed();
            } else if (parsedData.isSuccess()) {
                currentIndex = Math.min(parsedData.getEndIndex() + 1, input.length);
            }
        }
        return ParsedData.of(new String(input, offset, currentIndex - offset), currentIndex - 1);
    }
}
