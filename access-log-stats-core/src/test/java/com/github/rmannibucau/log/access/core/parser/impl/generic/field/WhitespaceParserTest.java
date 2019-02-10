package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class WhitespaceParserTest extends BaseDataParserTest {
    private final WhitespaceParser parser = new WhitespaceParser();

    @ParameterizedTest
    @MethodSource("data") // bug in junit csvsource parsing - it does a trim, using a manual parsing as workaround
    void parse(final String csv) {
        final String[] parts = csv.split(",");
        parse(parser, parts[0], parseInt(parts[1]), parseBoolean(parts[2]), null, parseInt(parts[3]));
    }

    static Stream<String> data() {
        return Stream.of(" ,0,true,0",
                "company.com ,11,true,11",
                "company.com other value,11,true,11",
                // invalid
                "a,0,false,-1",
                "1,0,false,-1",
                "@,0,false,-1");
    }
}
