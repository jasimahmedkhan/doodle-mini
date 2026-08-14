package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves one slot without exposing the repository outside the slot module. */
@Service
@Transactional(readOnly = true)
public class GetSlotUseCase {

    private final SlotRepository slotRepository;

    public GetSlotUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /** Returns the requested slot when it exists. */
    public TimeSlot get(SlotId slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
    }
}
