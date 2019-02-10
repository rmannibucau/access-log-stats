package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GluttonParserTest extends BaseDataParserTest {
    private final GluttonParser parser = new GluttonParser(true, "glouton", new char[]{ ' ', '|', '/' }, null);

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle, weird chars etc
            "123,0,true,123,2",
            "123|,0,true,123,2",
            "123|123,2,true,3,2",
            "123/123,2,true,3,2",
            "123 other value,0,true,123,2",
            "other 123,6,true,123,8",
            "other 123|,6,true,123,8",
            "other abc/,6,true,abc,8",
            "other 123 another,6,true,123,8",
            "other 123.@a another,6,true,123.@a,11",
            // invalid
            "|,0,false,-,-1"
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
