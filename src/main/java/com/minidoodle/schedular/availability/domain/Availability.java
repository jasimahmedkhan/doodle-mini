package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.TimeRange;

import java.util.List;
import java.util.Objects;

/** An ordered, clipped, and merged availability result for one requested window. */
public record Availability(List<AvailabilityRange> ranges) {

    public Availability {
        Objects.requireNonNull(ranges, "ranges must not be null");
        ranges = List.copyOf(ranges);
    }

    public List<TimeRange> freeRanges() {
        return rangesFor(SlotStatus.FREE);
    }

    public List<TimeRange> busyRanges() {
        return rangesFor(SlotStatus.BUSY);
    }

    public List<TimeRange> bookedRanges() {
        return rangesFor(SlotStatus.BOOKED);
    }

    private List<TimeRange> rangesFor(SlotStatus status) {
        return ranges.stream()
                .filter(range -> range.status() == status)
                .map(AvailabilityRange::timeRange)
                .toList();
    }
}
