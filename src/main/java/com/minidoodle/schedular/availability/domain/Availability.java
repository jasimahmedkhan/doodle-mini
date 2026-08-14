package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.TimeRange;

import java.util.List;

public record Availability(List<AvailabilityRange> ranges) {

    public Availability {
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
