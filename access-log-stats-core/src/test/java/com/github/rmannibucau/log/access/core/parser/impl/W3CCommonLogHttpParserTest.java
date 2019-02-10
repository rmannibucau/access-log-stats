package com.github.rmannibucau.log.access.core.parser.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import com.github.rmannibucau.log.access.core.parser.api.Line;
import com.github.rmannibucau.log.access.core.parser.api.Parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// complete cases
class W3CCommonLogHttpParserTest {
    private final Parser parser = new W3CCommonLogHttpParser();

    @ParameterizedTest
    @CsvSource({
            // valid - iterate of the line, end of the line, in the middle
            "true,ignored,127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 200 123,127.0.0.1,-,james,09/May/2018:16:00:39 +0000,GET /report HTTP/1.0,200,123"
            // todo
    })
    void parse(final boolean valid,
               final String error,
               final String input,
               final String hostOrIp,
               final String userRfc931,
               final String user,
               final String date,
               final String request,
               final String status,
               final String bytes) {
        final Line line = parser.parse(input.toCharArray());
        assertEquals(valid, line.isValid());

        final Map<String, String> data = line.getData();
        if (valid) {
            assertEquals(7, data.size());
            assertEquals(hostOrIp, data.get("remoteHost"));
            assertEquals(userRfc931, data.get("rfc931"));
            assertEquals(user, data.get("authuser"));
            assertEquals(date, data.get("date"));
            assertEquals(request, data.get("request"));
            assertEquals(status, data.get("status"));
            assertEquals(bytes, data.get("bytes"));
        } else {
            assertEquals(input, data.get("data"));
            assertEquals(error, data.get("error"));
        }
    }
}
