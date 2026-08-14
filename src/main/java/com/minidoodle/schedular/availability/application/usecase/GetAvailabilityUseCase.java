package com.minidoodle.schedular.availability.application.usecase;

import com.minidoodle.schedular.availability.domain.Availability;
import com.minidoodle.schedular.availability.domain.SlotStatus;
import com.minidoodle.schedular.availability.domain.SlotView;
import com.minidoodle.schedular.availability.domain.UserCalendar;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.SlotAvailabilityQuery;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;


@Service
@Transactional(readOnly = true)
public class GetAvailabilityUseCase {

    private final GetUserUseCase getUserUseCase;
    private final SlotAvailabilityQuery slotAvailabilityQuery;

    public GetAvailabilityUseCase(
            GetUserUseCase getUserUseCase,
            SlotAvailabilityQuery slotAvailabilityQuery
    ) {
        this.getUserUseCase = getUserUseCase;
        this.slotAvailabilityQuery = slotAvailabilityQuery;
    }

    public Availability get(UserId userId, Instant from, Instant to) {
        Objects.requireNonNull(userId, "userId must not be null");
        TimeRange window = new TimeRange(from, to);
        getUserUseCase.get(userId);

        var slots = slotAvailabilityQuery.findSlots(userId, from, to).stream()
                .map(GetAvailabilityUseCase::toAvailabilitySlotView)
                .toList();

        return new UserCalendar(slots).availability(window);
    }

    private static SlotView toAvailabilitySlotView(com.minidoodle.schedular.slot.application.SlotView slot) {
        return new SlotView(
                slot.slotId(),
                slot.timeRange(),
                SlotStatus.valueOf(slot.status().name())
        );
    }
}
