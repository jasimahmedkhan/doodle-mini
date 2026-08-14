package com.minidoodle.schedular.slot.application;

import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class SlotAvailabilityQueryImpl implements SlotAvailabilityQuery {

    private final SlotRepository slotRepository;

    public SlotAvailabilityQueryImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public List<SlotView> findSlots(UserId userId, Instant from, Instant to) {
        Objects.requireNonNull(userId, "userId must not be null");
        TimeRange window = new TimeRange(from, to);

        return slotRepository.findByOwnerAndOverlapping(userId, window).stream()
                .sorted(Comparator.comparing(slot -> slot.timeRange().start()))
                .map(SlotAvailabilityQueryImpl::toView)
                .toList();
    }

    private static SlotView toView(TimeSlot slot) {
        return new SlotView(
                slot.id(),
                slot.timeRange(),
                SlotView.Status.valueOf(slot.status().name())
        );
    }
}
