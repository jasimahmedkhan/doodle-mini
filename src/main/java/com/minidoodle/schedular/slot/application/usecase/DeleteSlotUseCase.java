package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class DeleteSlotUseCase {

    private final SlotRepository slotRepository;

    public DeleteSlotUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public void delete(SlotId slotId) {
        Objects.requireNonNull(slotId, "slotId must not be null");
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
        slot.assertModifiable();
        slotRepository.delete(slotId);
    }
}
