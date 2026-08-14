package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserCalendarTest {

    private static final Instant T08 = instant("08:00");
    private static final Instant T09 = instant("09:00");
    private static final Instant T10 = instant("10:00");
    private static final Instant T11 = instant("11:00");
    private static final Instant T12 = instant("12:00");
    private static final Instant T13 = instant("13:00");
    private static final Instant T14 = instant("14:00");

    @Test
    void returnsEmptyResultWhenWindowContainsNoSlots() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T08, T09, SlotStatus.FREE)
        ));

        Availability result = calendar.availability(range(T10, T11));

        assertEquals(List.of(), result.ranges());
        assertEquals(List.of(), result.freeRanges());
        assertEquals(List.of(), result.busyRanges());
        assertEquals(List.of(), result.bookedRanges());
    }

    @Test
    void clipsSlotsAtBothWindowBoundaries() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T08, T10, SlotStatus.FREE),
                slot(T12, T14, SlotStatus.BOOKED)
        ));

        Availability result = calendar.availability(range(T09, T13));

        assertEquals(List.of(
                availabilityRange(T09, T10, SlotStatus.FREE),
                availabilityRange(T12, T13, SlotStatus.BOOKED)
        ), result.ranges());
    }

    @Test
    void excludesSlotsThatOnlyTouchWindowBoundary() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T08, T09, SlotStatus.FREE),
                slot(T13, T14, SlotStatus.BOOKED)
        ));

        assertEquals(List.of(), calendar.availability(range(T09, T13)).ranges());
    }

    @Test
    void mergesAdjacentRangesWithTheSameStatus() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T09, T10, SlotStatus.FREE),
                slot(T10, T11, SlotStatus.FREE),
                slot(T11, T12, SlotStatus.FREE)
        ));

        Availability result = calendar.availability(range(T09, T13));

        assertEquals(List.of(
                availabilityRange(T09, T12, SlotStatus.FREE)
        ), result.ranges());
    }

    @Test
    void keepsAdjacentRangesWithDifferentStatusesSeparate() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T09, T10, SlotStatus.FREE),
                slot(T10, T11, SlotStatus.BUSY),
                slot(T11, T12, SlotStatus.BOOKED)
        ));

        Availability result = calendar.availability(range(T09, T13));

        assertEquals(List.of(
                availabilityRange(T09, T10, SlotStatus.FREE),
                availabilityRange(T10, T11, SlotStatus.BUSY),
                availabilityRange(T11, T12, SlotStatus.BOOKED)
        ), result.ranges());
    }

    @Test
    void ordersInterleavedStatusesAndProvidesStatusViews() {
        UserCalendar calendar = new UserCalendar(List.of(
                slot(T12, T13, SlotStatus.FREE),
                slot(T10, T11, SlotStatus.BOOKED),
                slot(T11, T12, SlotStatus.BUSY),
                slot(T09, T10, SlotStatus.FREE)
        ));

        Availability result = calendar.availability(range(T09, T14));

        assertEquals(List.of(
                availabilityRange(T09, T10, SlotStatus.FREE),
                availabilityRange(T10, T11, SlotStatus.BOOKED),
                availabilityRange(T11, T12, SlotStatus.BUSY),
                availabilityRange(T12, T13, SlotStatus.FREE)
        ), result.ranges());
        assertEquals(List.of(range(T09, T10), range(T12, T13)), result.freeRanges());
        assertEquals(List.of(range(T11, T12)), result.busyRanges());
        assertEquals(List.of(range(T10, T11)), result.bookedRanges());
    }

    private static SlotView slot(Instant start, Instant end, SlotStatus status) {
        return new SlotView(SlotId.random(), range(start, end), status);
    }

    private static AvailabilityRange availabilityRange(Instant start, Instant end, SlotStatus status) {
        return new AvailabilityRange(range(start, end), status);
    }

    private static TimeRange range(Instant start, Instant end) {
        return new TimeRange(start, end);
    }

    private static Instant instant(String time) {
        return Instant.parse("2026-08-14T" + time + ":00Z");
    }
}
