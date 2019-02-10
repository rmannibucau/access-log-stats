package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class Rfc1123HostnameSegmentTest extends BaseDataParserTest {
    private final Rfc1123HostnameParser parser = new Rfc1123HostnameParser(true);

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "company.com,0,true,company.com,10",
            "company.com,2,true,mpany.com,10",
            "company.com other value,0,true,company.com,10",
            "other company.com,6,true,company.com,16",
            "other company.com another,6,true,company.com,16",
            "something-more-complex.than.just-a.word.com,0,true,something-more-complex.than.just-a.word.com,42",
            // invalid
            "-company.com,0,false,-,-1",
            "company.com-,0,false,-,-1",
            ".company.com,0,false,-,-1",
            "company.com.,0,false,-,-1",
            // > 255
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz,0,false,-,-1",
    })
    void parse(final String input, final int offset, final boolean success, final String value, final int endIndex) {
        parse(parser, input, offset, success, value, endIndex);
    }
}
