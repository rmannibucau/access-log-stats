package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DigitsParserTest extends BaseDataParserTest {
    // for now we only test with 3 since it is our only usage
    private final DigitsParser parser = new DigitsParser(true, "digits", 3);

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "192,0,true,192,2",
            "192.168.123.123,2,true,2,2",
            "192 other value,0,true,192,2",
            "other 192,6,true,192,8",
            "other 192.,6,true,192,8",
            "other 192 another,6,true,192,8",
            "other 192. another,6,true,192,8",
            // invalid
            "company.com,0,false,-,-1"
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
