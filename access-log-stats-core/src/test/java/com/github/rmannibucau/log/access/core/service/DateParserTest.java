package com.github.rmannibucau.log.access.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;

import org.junit.jupiter.api.Test;

class DateParserTest {
    @Test
    void parse() {
        // if it fails we get current timestamp which is way after this one
        assertEquals(1525881641000L, new DateParser().parse("09/May/2018:16:00:41 +0000", Clock.systemUTC()).toEpochMilli());
    }
}
