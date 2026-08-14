package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.TimeRange;

import java.util.Objects;

public record AvailabilityRange(TimeRange timeRange, SlotStatus status) {

    public AvailabilityRange {
        Objects.requireNonNull(timeRange, "timeRange must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
