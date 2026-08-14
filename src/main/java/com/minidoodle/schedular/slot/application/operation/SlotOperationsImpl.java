package com.minidoodle.schedular.slot.application.operation;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class SlotOperationsImpl implements SlotOperations {

    private final SlotRepository slotRepository;

    public SlotOperationsImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public void reserve(SlotId slotId) {
        TimeSlot slot = findSlot(slotId);
        slot.book();
        slotRepository.save(slot);
    }

    @Override
    public void release(SlotId slotId) {
        TimeSlot slot = findSlot(slotId);
        slot.cancelBooking();
        slotRepository.save(slot);
    }

    private TimeSlot findSlot(SlotId slotId) {
        Objects.requireNonNull(slotId, "slotId must not be null");
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
    }
}
