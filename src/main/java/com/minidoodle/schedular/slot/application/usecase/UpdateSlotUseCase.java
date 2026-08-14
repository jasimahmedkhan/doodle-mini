package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import com.minidoodle.schedular.slot.domain.exception.SlotOverlapException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class UpdateSlotUseCase {

    private final SlotRepository slotRepository;

    public UpdateSlotUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public TimeSlot update(SlotId slotId, TimeRange timeRange) {
        Objects.requireNonNull(slotId, "slotId must not be null");
        Objects.requireNonNull(timeRange, "timeRange must not be null");

        TimeSlot slot = findSlot(slotId);
        slot.assertModifiable();

        boolean overlapsAnotherSlot = slotRepository
                .findByOwnerAndOverlapping(slot.owner(), timeRange)
                .stream()
                .anyMatch(existing -> !existing.id().equals(slotId));
        if (overlapsAnotherSlot) {
            throw new SlotOverlapException("Updated slot overlaps another slot for user " + slot.owner());
        }

        slot.update(timeRange);
        return slotRepository.save(slot);
    }

    private TimeSlot findSlot(SlotId slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
    }
}
