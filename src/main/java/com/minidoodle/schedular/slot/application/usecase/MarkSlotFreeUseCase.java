package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Returns a manually BUSY slot to FREE. */
@Service
@Transactional
public class MarkSlotFreeUseCase {

    private final SlotRepository slotRepository;

    public MarkSlotFreeUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /** Applies the BUSY-to-FREE state transition and persists it. */
    public TimeSlot markFree(SlotId slotId) {
        Objects.requireNonNull(slotId, "slotId must not be null");
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
        slot.markFree();
        return slotRepository.save(slot);
    }
}
