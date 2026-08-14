package com.minidoodle.schedular.meeting.application.support;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;

import java.util.Objects;

public final class FailingSlotOperations implements SlotOperations {

    private final RuntimeException reserveFailure;

    public FailingSlotOperations(RuntimeException reserveFailure) {
        this.reserveFailure = Objects.requireNonNull(reserveFailure, "reserveFailure must not be null");
    }

    @Override
    public void reserve(SlotId slotId) {
        throw reserveFailure;
    }

    @Override
    public void release(SlotId slotId) {
    }
}
