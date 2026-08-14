package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.TimeRange;

public record AvailabilityRange(TimeRange timeRange, SlotStatus status) {
}
