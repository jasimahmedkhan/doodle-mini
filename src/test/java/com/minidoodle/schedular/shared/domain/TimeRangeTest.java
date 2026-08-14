package com.minidoodle.schedular.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimeRangeTest {

    private static final Instant T1 = Instant.parse("2026-08-14T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-08-14T11:00:00Z");
    private static final Instant T3 = Instant.parse("2026-08-14T12:00:00Z");
    private static final Instant T4 = Instant.parse("2026-08-14T13:00:00Z");

    @Test
    void rejectsNullBoundaries() {
        assertThrows(NullPointerException.class, () -> new TimeRange(null, T2));
        assertThrows(NullPointerException.class, () -> new TimeRange(T1, null));
    }

    @Test
    void rejectsEqualStartAndEnd() {
        assertThrows(IllegalArgumentException.class, () -> new TimeRange(T1, T1));
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> new TimeRange(T2, T1));
    }

    @Test
    void calculatesDuration() {
        TimeRange range = new TimeRange(T1, T2);
        assertEquals(Duration.ofHours(1), range.duration());
    }

    @Test
    void overlapsWhenRangesIntersect() {
        TimeRange a = new TimeRange(T1, T3); // 10-12
        TimeRange b = new TimeRange(T2, T4); // 11-13
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    void doesNotOverlapWhenRangesAreAdjacent() {
        TimeRange a = new TimeRange(T1, T2); // 10-11
        TimeRange b = new TimeRange(T2, T3); // 11-12
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void doesNotOverlapWhenRangesAreSeparate() {
        TimeRange a = new TimeRange(T1, T2); // 10-11
        TimeRange b = new TimeRange(T3, T4); // 12-13
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void overlapsWhenOneContainsTheOther() {
        TimeRange a = new TimeRange(T1, T4); // 10-13
        TimeRange b = new TimeRange(T2, T3); // 11-12
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    void equalRangesAreEqualAndHaveSameHashCode() {
        TimeRange a = new TimeRange(T1, T2);
        TimeRange b = new TimeRange(T1, T2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentRangesAreNotEqual() {
        TimeRange a = new TimeRange(T1, T2);
        TimeRange b = new TimeRange(T1, T3);
        assertNotEquals(a, b);
    }
}
