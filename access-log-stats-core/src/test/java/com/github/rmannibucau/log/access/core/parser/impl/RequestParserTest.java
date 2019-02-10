package com.github.rmannibucau.log.access.core.parser.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import com.github.rmannibucau.log.access.core.parser.api.Line;
import com.github.rmannibucau.log.access.core.parser.api.Parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// complete cases
class RequestParserTest {
    private final Parser parser = new RequestParser();

    @ParameterizedTest
    @CsvSource({
            "true,-,GET /report HTTP/1.0,GET,/report,HTTP/1.0",
            "true,-,POST /report/morecomplicated.jsp?foo=var HTTP/1.1,POST,/report/morecomplicated.jsp?foo=var,HTTP/1.1"
    })
    void parse(final boolean valid,
               final String error,
               final String input,
               final String method,
               final String path,
               final String protocol) {
        final Line line = parser.parse(input.toCharArray());
        assertEquals(valid, line.isValid());

        final Map<String, String> data = line.getData();
        if (valid) {
            assertEquals(3, data.size());
            assertEquals(method, data.get("method"));
            assertEquals(path, data.get("path"));
            assertEquals(protocol, data.get("protocol"));
        } else {
            assertEquals(input, data.get("data"));
            assertEquals(error, data.get("error"));
        }
    }
}
