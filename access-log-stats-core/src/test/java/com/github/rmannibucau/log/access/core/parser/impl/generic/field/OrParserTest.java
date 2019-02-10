package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OrParserTest extends BaseDataParserTest {
    // mainly the one we use in our impl so testing it in priority
    private final DataParser parser = new OrParser("test", true,
            new Rfc1123HostnameParser(true), new IP4Parser(true));

    @ParameterizedTest
    @CsvSource({ // took some of the datasets of hostname and ip parser, we don't need all of them though
            // valid
            "company.com,0,true,company.com,10",
            "company.com,2,true,mpany.com,10",
            "company.com other value,0,true,company.com,10",
            "other company.com,6,true,company.com,16",
            "other company.com another,6,true,company.com,16",
            "something-more-complex.than.just-a.word.com,0,true,something-more-complex.than.just-a.word.com,42",
            "192.168.123.123,0,true,192.168.123.123,14",
            "192.168.123.123,2,true,2.168.123.123,14",
            "192.168.123.123 other value,0,true,192.168.123.123,14",
            "other 192.168.123.123,6,true,192.168.123.123,20",
            "other 192.168.123.123 another,6,true,192.168.123.123,20",
            // invalid
            "-company.com,0,false,-,-1",
            "company.com-,0,false,-,-1",
            ".company.com,0,false,-,-1",
            "company.com.,0,false,-,-1",
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz,0,false,-,-1",
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
