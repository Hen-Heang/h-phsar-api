package com.henheang.hphsar.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public final class DateTimeUtil {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN).withZone(DEFAULT_ZONE_ID);

    /**
     * Parsing accepts an optional fractional-second part on top of {@link #PATTERN};
     * {@link #format(LocalDateTime)} still emits whole seconds only.
     * <p>
     * PostgreSQL {@code timestamp} columns are read into String model fields
     * (e.g. {@code Order.date}) and arrive with microsecond precision — for
     * example {@code 2026-09-02 08:39:14.297922}. The services normalize those
     * through {@code format(parse(...))}, so parsing with the strict output
     * pattern alone made every one of those call sites throw
     * {@link IllegalArgumentException} for any row that did not land exactly on
     * a whole second.
     */
    private static final DateTimeFormatter LENIENT_PARSER = new DateTimeFormatterBuilder()
            .appendPattern(PATTERN)
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter()
            .withZone(DEFAULT_ZONE_ID);

    private DateTimeUtil() {

    }

    // LocalDateTime -> String
    public static String format(LocalDateTime dateTime) {

        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);

    }

    // String -> LocalDateTime
    public static LocalDateTime parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, LENIENT_PARSER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Expected: " + PATTERN + ", value: " + text, e);
        }
    }
}
