package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.ParsedData;

/**
 * Reusable segment parsing a hostname using rfc1123.
 */
public class Rfc1123HostnameParser extends BaseParser {
    private final boolean required;

    /**
     * @param required is this segment required or not.
     */
    public Rfc1123HostnameParser(final boolean required) {
        this.required = required;
    }

    @Override
    public String name() {
        return "hostname";
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    protected boolean isValidCharacter(final char value) {
        // only ASCII a-z characters case insensitively
        return (value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z')
                // numbers
                || (value >= '0' && value <= '9')
                // or hypen
                || value == '-'
                // or dots
                || value == '.';
    }

    @Override
    public ParsedData read(final char[] input, final int offset) {
        final ParsedData data = super.read(input, offset);
        if (!data.isSuccess()) {
            return data;
        }
        final String value = data.getData();
        // a hostname must be < 255 characters
        if (value.length() > 255) {
            return ParsedData.failed();
        }
        // a hostname must not iterate or end with an hyphen or a dot
        if (startsOrEndsWith(value, '-')) {
            return ParsedData.failed();
        }
        if (startsOrEndsWith(value, '.')) {
            return ParsedData.failed();
        }
        return data;
    }

    private boolean startsOrEndsWith(final String data, final char value) {
        return data.charAt(0) == value || data.charAt(data.length() - 1) == value;
    }
}
