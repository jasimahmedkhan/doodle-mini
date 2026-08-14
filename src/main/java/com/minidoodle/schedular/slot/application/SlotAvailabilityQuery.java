package com.minidoodle.schedular.slot.application;

import com.minidoodle.schedular.shared.domain.UserId;

import java.time.Instant;
import java.util.List;

public interface SlotAvailabilityQuery {

    List<SlotView> findSlots(UserId userId, Instant from, Instant to);
}
