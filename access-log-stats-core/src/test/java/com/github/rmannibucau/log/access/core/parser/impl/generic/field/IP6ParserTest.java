package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IP6ParserTest extends BaseDataParserTest {
    private final DataParser parser = new IP6Parser(true);

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "2001:0db8:0a0b:12f0:0000:0000:0000:0001,0,true,2001:0db8:0a0b:12f0:0000:0000:0000:0001,38",
            "2001:0db8:0a0b:12f0:0000:0000:0000:0001,2,true,01:0db8:0a0b:12f0:0000:0000:0000:0001,38",
            "2001:0db8:0a0b:12f0:0000:0000:0000:0001 other value,0,true,2001:0db8:0a0b:12f0:0000:0000:0000:0001,38",
            "FE80:0000:0000:0000:0202:B3FF:FE1E:8329,0,true,FE80:0000:0000:0000:0202:B3FF:FE1E:8329,38",
            "other 2001:0db8:0a0b:12f0:0000:0000:0000:0001,6,true,2001:0db8:0a0b:12f0:0000:0000:0000:0001,44",
            "other 2001:0db8:0a0b:12f0:0000:0000:0000:0001 another,6,true,2001:0db8:0a0b:12f0:0000:0000:0000:0001,44",
            "other [2001:0db8:0a0b:12f0:0000:0000:0000:0001] another,6,true,[2001:0db8:0a0b:12f0:0000:0000:0000:0001],46",
            "other [2001:0db8:0a0b:12f0:0000:0000:0000:0001]:1234 another,6,true,[2001:0db8:0a0b:12f0:0000:0000:0000:0001]:1234,51",
            // invalid
            "company.com,0,false,-,-1",
            "GE80:0000:0000:0000:0202:B3FF:FE1E:8329,0,false,-,-1"
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
