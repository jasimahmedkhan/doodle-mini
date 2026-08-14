package com.minidoodle.schedular.meeting.application.support;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;
import com.minidoodle.schedular.slot.domain.TimeSlot;

import java.util.Objects;

public final class InMemorySlotOperations implements SlotOperations {

    private final TimeSlot slot;

    public InMemorySlotOperations(TimeSlot slot) {
        this.slot = Objects.requireNonNull(slot, "slot must not be null");
    }

    @Override
    public void reserve(SlotId slotId) {
        requireMatchingSlot(slotId);
        slot.book();
    }

    @Override
    public void release(SlotId slotId) {
        requireMatchingSlot(slotId);
        slot.cancelBooking();
    }

    private void requireMatchingSlot(SlotId slotId) {
        if (!slot.id().equals(slotId)) {
            throw new IllegalArgumentException("Unexpected slot: " + slotId);
        }
    }
}
