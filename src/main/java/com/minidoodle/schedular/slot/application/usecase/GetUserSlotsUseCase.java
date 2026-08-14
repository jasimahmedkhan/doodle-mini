package com.minidoodle.schedular.slot.application.usecase;

import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Returns raw, individually addressable slots for a user's bounded time window. */
@Service
@Transactional(readOnly = true)
public class GetUserSlotsUseCase {

    private static final Comparator<TimeSlot> BY_START_AND_ID = Comparator
            .comparing((TimeSlot slot) -> slot.timeRange().start())
            .thenComparing(slot -> slot.id().value());

    private final SlotRepository slotRepository;

    public GetUserSlotsUseCase(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /** Returns every intersecting slot in deterministic order without merging adjacent slots. */
    public List<TimeSlot> get(UserId owner, TimeRange window) {
        return slotRepository.findByOwnerAndOverlapping(owner, window).stream()
                .sorted(BY_START_AND_ID)
                .toList();
    }
}
