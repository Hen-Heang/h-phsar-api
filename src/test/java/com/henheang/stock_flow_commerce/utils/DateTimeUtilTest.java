package com.henheang.stock_flow_commerce.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeUtilTest {

    @Test
    void format_ShouldMatchExpectedPattern() {
        LocalDateTime value = LocalDateTime.of(2026, 2, 27, 14, 35, 12);

        String result = DateTimeUtil.format(value);

        assertEquals("2026-02-27 14:35:12", result);
    }

    @Test
    void parse_ShouldReturnExpectedLocalDateTime() {
        LocalDateTime result = DateTimeUtil.parse("2026-02-27 14:35:12");

        assertEquals(LocalDateTime.of(2026, 2, 27, 14, 35, 12), result);
    }

    @Test
    void parseAndFormat_ShouldRoundTrip() {
        String text = "2026-02-27 14:35:12";

        String result = DateTimeUtil.format(DateTimeUtil.parse(text));

        assertEquals(text, result);
    }

    @Test
    void format_ShouldReturnNull_WhenInputIsNull() {
        assertNull(DateTimeUtil.format((LocalDateTime) null));
    }

    @Test
    void parse_ShouldReturnNull_WhenInputIsNullOrBlank() {
        assertNull(DateTimeUtil.parse(null));
        assertNull(DateTimeUtil.parse(""));
        assertNull(DateTimeUtil.parse("   "));
    }

    @Test
    void parse_ShouldThrowIllegalArgumentException_WhenPatternIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.parse("27/02/2026 14:35:12"));
    }
}
