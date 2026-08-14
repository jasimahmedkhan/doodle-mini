package com.minidoodle.schedular.slot.application.operation;

import com.minidoodle.schedular.shared.domain.SlotId;

/**
 * Public write contract for modules that need to reserve or release a slot.
 * It exposes state transitions without exposing the slot aggregate or repository.
 */
public interface SlotOperations {

    /** Moves a FREE slot to BOOKED. */
    void reserve(SlotId slotId);

    /** Moves a BOOKED slot back to FREE after its meeting is cancelled. */
    void release(SlotId slotId);
}
