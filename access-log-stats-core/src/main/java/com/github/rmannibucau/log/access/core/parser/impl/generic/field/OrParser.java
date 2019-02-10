package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import static java.util.Objects.requireNonNull;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

/**
 * Enables to parse a field using one or another parser.
 *
 * Ex: hostname or ip.
 *
 * TODO: have conditional order, ex: if length == 16 use parser1 else parser2 etc
 */
public class OrParser implements DataParser {
    private final String name;
    private final boolean required;
    private final DataParser preferred;
    private final DataParser fallback;

    public OrParser(final String name, final boolean required,
                    final DataParser preferred, final DataParser fallback) {
        this.name = name;
        this.required = required;
        this.preferred = requireNonNull(preferred, "parsers can't be null (preferred)");
        this.fallback = requireNonNull(fallback, "parsers can't be null (fallback)");
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
        final ParsedData data = preferred.read(input, offset);
        if (data.isSuccess()) {
            return data;
        }
        return fallback.read(input, offset);
    }
}
