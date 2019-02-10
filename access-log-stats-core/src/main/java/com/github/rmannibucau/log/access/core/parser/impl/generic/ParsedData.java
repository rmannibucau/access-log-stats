package com.github.rmannibucau.log.access.core.parser.impl.generic;

public class ParsedData {
    private static final ParsedData FAILED = new ParsedData(false, null, -1);

    private final String data;
    private final int endIndex;
    private final boolean success;

    private ParsedData(final boolean success, final String data, final int endIndex) {
        this.data = data;
        this.success = success;
        this.endIndex = endIndex;
        if (success && endIndex < 0) {
            throw new IllegalArgumentException("A successfully parsed field must set next index and it must be >= 0");
        }
    }

    public String getData() {
        return data;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public boolean isSuccess() {
        return success;
    }

    public static ParsedData of(final String data, final int endOffset) {
        return new ParsedData(true, data, endOffset);
    }

    public static ParsedData failed() {
        return FAILED;
    }
}
