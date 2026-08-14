package com.minidoodle.schedular.slot.application;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;
import com.minidoodle.schedular.slot.application.operation.SlotOperationsImpl;
import com.minidoodle.schedular.slot.application.support.FakeSlotRepository;
import com.minidoodle.schedular.slot.application.usecase.CreateSlotUseCase;
import com.minidoodle.schedular.slot.application.usecase.DeleteSlotUseCase;
import com.minidoodle.schedular.slot.application.usecase.UpdateSlotUseCase;
import com.minidoodle.schedular.slot.domain.SlotStatus;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotModifiableException;
import com.minidoodle.schedular.slot.domain.exception.SlotOverlapException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotApplicationTest {

    private static final Instant T09 = instant("09:00");
    private static final Instant T10 = instant("10:00");
    private static final Instant T11 = instant("11:00");
    private static final Instant T12 = instant("12:00");
    private static final Instant T13 = instant("13:00");

    @Test
    void createRejectsAnOverlappingSlot() {
        UserId owner = UserId.random();
        FakeSlotRepository slots = new FakeSlotRepository();
        slots.save(slot(owner, T09, T11, SlotStatus.FREE));

        assertThrows(
                SlotOverlapException.class,
                () -> new CreateSlotUseCase(slots).create(owner, range(T10, T12))
        );
    }

    @Test
    void updateIgnoresTheSlotBeingUpdatedDuringOverlapCheck() {
        UserId owner = UserId.random();
        FakeSlotRepository slots = new FakeSlotRepository();
        TimeSlot slot = slots.save(slot(owner, T09, T10, SlotStatus.FREE));

        TimeSlot updated = new UpdateSlotUseCase(slots).update(slot.id(), range(T09, T11));

        assertEquals(range(T09, T11), updated.timeRange());
    }

    @Test
    void deleteGuardsBookedSlots() {
        FakeSlotRepository slots = new FakeSlotRepository();
        TimeSlot slot = slots.save(slot(UserId.random(), T09, T10, SlotStatus.BOOKED));

        assertThrows(SlotNotModifiableException.class, () -> new DeleteSlotUseCase(slots).delete(slot.id()));
        assertTrue(slots.findById(slot.id()).isPresent());
    }

    @Test
    void slotOperationsReserveAndRelease() {
        FakeSlotRepository slots = new FakeSlotRepository();
        TimeSlot slot = slots.save(slot(UserId.random(), T09, T10, SlotStatus.FREE));
        SlotOperations operations = new SlotOperationsImpl(slots);

        operations.reserve(slot.id());
        assertTrue(slot.isBooked());
        operations.release(slot.id());
        assertTrue(slot.isFree());
    }

    @Test
    void availabilityQueryReturnsOrderedBoundaryProjection() {
        UserId owner = UserId.random();
        FakeSlotRepository slots = new FakeSlotRepository();
        TimeSlot later = slots.save(slot(owner, T11, T12, SlotStatus.BUSY));
        TimeSlot earlier = slots.save(slot(owner, T09, T10, SlotStatus.FREE));
        slots.save(slot(UserId.random(), T09, T10, SlotStatus.BOOKED));

        List<SlotView> result = new SlotAvailabilityQueryImpl(slots).findSlots(owner, T09, T13);

        assertEquals(List.of(earlier.id(), later.id()), result.stream().map(SlotView::slotId).toList());
        assertEquals(List.of(SlotView.Status.FREE, SlotView.Status.BUSY),
                result.stream().map(SlotView::status).toList());
    }

    private static TimeSlot slot(UserId owner, Instant start, Instant end, SlotStatus status) {
        return new TimeSlot(SlotId.random(), owner, range(start, end), status);
    }

    private static TimeRange range(Instant start, Instant end) {
        return new TimeRange(start, end);
    }

    private static Instant instant(String time) {
        return Instant.parse("2026-08-14T" + time + ":00Z");
    }

}
