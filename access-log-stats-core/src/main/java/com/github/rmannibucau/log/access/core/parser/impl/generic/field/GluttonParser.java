package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import java.util.Arrays;
import java.util.function.Predicate;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

public class GluttonParser implements DataParser {
    private final boolean required;
    private final String name;
    private final Predicate<Character> stopMatcher;
    private final Predicate<Character> validator;

    public GluttonParser(final boolean required, final String name,
                         final char[] stopChars, final char[] whitelistChars) {
        this.required = required;
        this.name = name;
        if (stopChars.length == 0) {
            throw new IllegalArgumentException("You can't use glouton parser without stop characters");
        }
        if (stopChars.length == 1) { // optimized
            this.stopMatcher = c -> c == stopChars[0];
        } else {
            final char[] copy = new char[stopChars.length];
            System.arraycopy(stopChars, 0, copy, 0, stopChars.length);
            Arrays.sort(copy); // for binarySearch
            this.stopMatcher = c -> Arrays.binarySearch(copy, c) >= 0;
        }
        if (whitelistChars == null || whitelistChars.length == 0) {
            validator = i -> true;
        } else {
            final char[] copy = new char[whitelistChars.length];
            System.arraycopy(whitelistChars, 0, copy, 0, whitelistChars.length);
            Arrays.sort(copy);
            this.validator = c -> Arrays.binarySearch(copy, c) >= 0;
        }
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
        for (int i = offset; i < input.length; i++) {
            if (stopMatcher.test(input[i])) {
                if (i == offset) {
                    if (!required) {
                        return ParsedData.of("", offset);
                    }
                    return ParsedData.failed();
                }
                i--; // ignore stop char
                return ParsedData.of(new String(input, offset, i - offset + 1), i);
            } else if (!validator.test(input[i])) {
                return ParsedData.failed();
            }
        }
        return ParsedData.of(new String(input, offset, input.length - offset), input.length - 1);
    }
}
