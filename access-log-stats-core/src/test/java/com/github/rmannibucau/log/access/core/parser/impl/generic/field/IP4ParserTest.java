package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IP4ParserTest extends BaseDataParserTest {
    private final DataParser parser = new IP4Parser(true);

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "192.168.123.123,0,true,192.168.123.123,14",
            "192.168.123.123,2,true,2.168.123.123,14",
            "192.168.123.123 other value,0,true,192.168.123.123,14",
            "other 192.168.123.123,6,true,192.168.123.123,20",
            "other 192.168.123.123 another,6,true,192.168.123.123,20",
            // invalid
            "company.com,0,false,-,-1"
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
