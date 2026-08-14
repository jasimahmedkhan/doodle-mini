package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;

import java.util.Objects;

public record SlotView(SlotId slotId, TimeRange timeRange, SlotStatus status) {

    public SlotView {
        Objects.requireNonNull(slotId, "slotId must not be null");
        Objects.requireNonNull(timeRange, "timeRange must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
