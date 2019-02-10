package com.github.rmannibucau.log.access.core.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DateParser {
    // 09/May/2018:16:00:41 +0000
    private final DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ofPattern(
            "dd/LLL/yyyy:HH:mm:ss Z", Locale.getDefault());

    // todo: enhance with more patterns and use the first matching one
    public Instant parse(final String value, final Clock clock) {
        try {
            final ZonedDateTime dateTime = ZonedDateTime.parse(value, defaultDateFormatter);
            return dateTime.toInstant();
        } catch (final DateTimeParseException dtpe) {
            return Instant.now(clock); // fallback if we can't parse it, hopefully not too wrong
        }
    }
}
