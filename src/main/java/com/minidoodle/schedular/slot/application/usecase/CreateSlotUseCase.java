package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.SlotStatus;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotOverlapException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateSlotUseCase {

    private final SlotRepository slotRepository;

    public CreateSlotUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public TimeSlot create(UserId owner, TimeRange timeRange) {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(timeRange, "timeRange must not be null");

        if (slotRepository.existsOverlapping(owner, timeRange)) {
            throw new SlotOverlapException("Slot overlaps another slot for user " + owner);
        }

        TimeSlot slot = new TimeSlot(SlotId.random(), owner, timeRange, SlotStatus.FREE);
        return slotRepository.save(slot);
    }
}
