package com.henheang.stock_flow_commerce.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN).withZone(DEFAULT_ZONE_ID);

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
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Expected: " + PATTERN + ", value: " + text, e);
        }
    }
}
