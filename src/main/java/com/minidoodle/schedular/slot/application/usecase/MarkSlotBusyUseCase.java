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
public class MarkSlotBusyUseCase {

    private final SlotRepository slotRepository;

    public MarkSlotBusyUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public TimeSlot markBusy(SlotId slotId) {
        Objects.requireNonNull(slotId, "slotId must not be null");
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
        slot.markBusy();
        return slotRepository.save(slot);
    }
}
