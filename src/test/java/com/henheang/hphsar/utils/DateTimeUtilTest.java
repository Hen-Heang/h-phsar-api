package com.henheang.hphsar.utils;

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

    @Test
    void parse_ShouldAcceptFractionalSeconds_AsReadFromPostgresTimestampColumns() {
        // PostgreSQL renders timestamp columns with microsecond precision, and the
        // services read those into String model fields (e.g. Order.date) before
        // normalizing them through format(parse(...)). Rejecting the fraction made
        // every such call site throw for any row not landing on a whole second.
        assertEquals(LocalDateTime.of(2026, 2, 27, 14, 35, 12, 297_922_000),
                DateTimeUtil.parse("2026-02-27 14:35:12.297922"));
        assertEquals(LocalDateTime.of(2026, 2, 27, 14, 35, 12, 100_000_000),
                DateTimeUtil.parse("2026-02-27 14:35:12.1"));
        assertEquals(LocalDateTime.of(2026, 2, 27, 14, 35, 12, 123_456_789),
                DateTimeUtil.parse("2026-02-27 14:35:12.123456789"));
    }

    @Test
    void parseAndFormat_ShouldTruncateFractionalSecondsToTheResponsePattern() {
        // The normalization round trip is what the services rely on to hand clients
        // a stable "yyyy-MM-dd HH:mm:ss" string; the fraction must be dropped, not
        // rejected. This is the exact value that broke OrderLifecycleIT in CI.
        assertEquals("2026-09-02 08:39:14",
                DateTimeUtil.format(DateTimeUtil.parse("2026-09-02 08:39:14.297922")));
    }

    @Test
    void parse_ShouldStillRejectTrailingGarbageAfterTheSeconds() {
        // Accepting a fraction must not turn parse() into "ignore whatever follows".
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.parse("2026-02-27 14:35:12.abc"));
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.parse("2026-02-27 14:35:12 extra"));
    }
}
