package com.minidoodle.schedular.availability.application.support;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.SlotAvailabilityQuery;
import com.minidoodle.schedular.slot.application.SlotView;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class FakeSlotAvailabilityQuery implements SlotAvailabilityQuery {

    private final List<SlotView> slots;
    private boolean queried;

    public FakeSlotAvailabilityQuery(List<SlotView> slots) {
        this.slots = List.copyOf(Objects.requireNonNull(slots, "slots must not be null"));
    }

    @Override
    public List<SlotView> findSlots(UserId userId, Instant from, Instant to) {
        queried = true;
        return slots;
    }

    public boolean wasQueried() {
        return queried;
    }
}
