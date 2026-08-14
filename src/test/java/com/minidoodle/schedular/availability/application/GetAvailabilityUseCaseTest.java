package com.minidoodle.schedular.availability.application;

import com.minidoodle.schedular.availability.application.support.SingleUserRepository;
import com.minidoodle.schedular.availability.application.support.FakeSlotAvailabilityQuery;
import com.minidoodle.schedular.availability.application.usecase.GetAvailabilityUseCase;
import com.minidoodle.schedular.availability.domain.Availability;
import com.minidoodle.schedular.availability.domain.AvailabilityRange;
import com.minidoodle.schedular.availability.domain.SlotStatus;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.SlotView;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.exception.UserNotFoundException;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetAvailabilityUseCaseTest {

    private static final Instant T09 = instant("09:00");
    private static final Instant T10 = instant("10:00");
    private static final Instant T11 = instant("11:00");
    private static final Instant T12 = instant("12:00");

    @Test
    void verifiesUserAndReturnsMergedAvailabilityForFullWindow() {
        User user = new User(UserId.random(), "Alice", "alice@example.com");
        GetUserUseCase getUser = new GetUserUseCase(new SingleUserRepository(user));
        var slots = new FakeSlotAvailabilityQuery(List.of(
                new SlotView(SlotId.random(), range(T09, T10), SlotView.Status.FREE),
                new SlotView(SlotId.random(), range(T10, T11), SlotView.Status.FREE),
                new SlotView(SlotId.random(), range(T11, T12), SlotView.Status.BOOKED)
        ));

        Availability result = new GetAvailabilityUseCase(getUser, slots).get(user.id(), T09, T12);

        assertEquals(List.of(
                new AvailabilityRange(range(T09, T11), SlotStatus.FREE),
                new AvailabilityRange(range(T11, T12), SlotStatus.BOOKED)
        ), result.ranges());
    }

    @Test
    void doesNotQuerySlotsWhenUserDoesNotExist() {
        var slots = new FakeSlotAvailabilityQuery(List.of());
        GetUserUseCase getUser = new GetUserUseCase(new SingleUserRepository(null));

        assertThrows(
                UserNotFoundException.class,
                () -> new GetAvailabilityUseCase(getUser, slots).get(UserId.random(), T09, T12)
        );
        assertFalse(slots.wasQueried());
    }

    private static TimeRange range(Instant start, Instant end) {
        return new TimeRange(start, end);
    }

    private static Instant instant(String time) {
        return Instant.parse("2026-08-14T" + time + ":00Z");
    }

}
