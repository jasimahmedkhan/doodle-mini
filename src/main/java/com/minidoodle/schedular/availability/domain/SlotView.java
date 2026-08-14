package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;

public record SlotView(SlotId slotId, TimeRange timeRange, SlotStatus status) {
}
