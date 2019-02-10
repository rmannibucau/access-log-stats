package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CharParserTest extends BaseDataParserTest {
    private final CharParser parser = new CharParser(true, '.');

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "192.,3,true,3",
            ".dsede,0,true,0",
            "dse.de,3,true,3",
            // invalid
            "whatever,0,false,-1"
    })
    void parse(final String input, final int offset, final boolean success, final int endIndex) {
        parse(parser, input, offset, success, null, endIndex);
    }
}
