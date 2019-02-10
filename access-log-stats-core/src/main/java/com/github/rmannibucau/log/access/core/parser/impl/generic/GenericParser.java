package com.github.rmannibucau.log.access.core.parser.impl.generic;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.rmannibucau.log.access.core.parser.api.Line;
import com.github.rmannibucau.log.access.core.parser.api.Parser;

/**
 * Generic parser iterating over a buffer.
 */
public class GenericParser implements Parser {
    private final DataParser[] fields;

    public GenericParser(final DataParser[] fields) {
        this.fields = requireNonNull(fields, "You can't parse logs without data");
    }

    @Override
    public Line parse(final char[] input) {
        int currentIndex = 0;
        final Map<String, String> values = new HashMap<>();
        for (final DataParser field : fields) {
            if (currentIndex >= input.length) {
                if (field.isRequired()) {
                    return Line.failed(new String(input), "Missing field '" + field.name() + "'");
                }
                continue; // else ok, let's check next field
            }

            final ParsedData parsedData = field.read(input, currentIndex);
            if (!parsedData.isSuccess() && field.isRequired()) {
                return Line.failed(new String(input), "Missing field '" + field.name() + "'");
            } else if (parsedData.isSuccess()) {
                if (parsedData.getData() != null) {
                    values.put(field.name(), parsedData.getData());
                }
                currentIndex = Math.min(parsedData.getEndIndex() + 1, input.length);
            }
        }
        if (currentIndex < input.length - 1) {
            return Line.failed(new String(input), "Some part of the input was not parsed");
        }
        return Line.of(values);
    }

    @Override
    public String toString() {
        return Arrays.stream(fields).map(DataParser::name).collect(joining());
    }
}
