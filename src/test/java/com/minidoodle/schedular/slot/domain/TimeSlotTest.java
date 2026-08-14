package com.minidoodle.schedular.slot.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.exception.SlotNotBookableException;
import com.minidoodle.schedular.slot.domain.exception.SlotNotModifiableException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    private static final Instant T1 = Instant.parse("2026-08-14T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-08-14T11:00:00Z");
    private static final Instant T3 = Instant.parse("2026-08-14T12:00:00Z");

    private static TimeSlot freeSlot() {
        return new TimeSlot(
                new SlotId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new TimeRange(T1, T2),
                SlotStatus.FREE
        );
    }

    private static TimeSlot busySlot() {
        return new TimeSlot(
                new SlotId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new TimeRange(T1, T2),
                SlotStatus.BUSY
        );
    }

    private static TimeSlot bookedSlot() {
        return new TimeSlot(
                new SlotId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new TimeRange(T1, T2),
                SlotStatus.BOOKED
        );
    }

    @Test
    void bookTransitionsFreeToBooked() {
        TimeSlot slot = freeSlot();
        slot.book();
        assertEquals(SlotStatus.BOOKED, slot.status());
        assertTrue(slot.isBooked());
    }

    @Test
    void bookThrowsWhenBusy() {
        TimeSlot slot = busySlot();
        assertThrows(SlotNotBookableException.class, slot::book);
    }

    @Test
    void bookThrowsWhenAlreadyBooked() {
        TimeSlot slot = bookedSlot();
        assertThrows(SlotNotBookableException.class, slot::book);
    }

    @Test
    void cancelBookingTransitionsBookedToFree() {
        TimeSlot slot = bookedSlot();
        slot.cancelBooking();
        assertEquals(SlotStatus.FREE, slot.status());
        assertTrue(slot.isFree());
    }

    @Test
    void cancelBookingThrowsWhenFree() {
        TimeSlot slot = freeSlot();
        assertThrows(SlotNotBookableException.class, slot::cancelBooking);
    }

    @Test
    void cancelBookingThrowsWhenBusy() {
        TimeSlot slot = busySlot();
        assertThrows(SlotNotBookableException.class, slot::cancelBooking);
    }

    @Test
    void markBusyTransitionsFreeToBusy() {
        TimeSlot slot = freeSlot();
        slot.markBusy();
        assertEquals(SlotStatus.BUSY, slot.status());
        assertTrue(slot.isBusy());
    }

    @Test
    void markBusyThrowsWhenBusy() {
        TimeSlot slot = busySlot();
        assertThrows(SlotNotModifiableException.class, slot::markBusy);
    }

    @Test
    void markBusyThrowsWhenBooked() {
        TimeSlot slot = bookedSlot();
        assertThrows(SlotNotModifiableException.class, slot::markBusy);
    }

    @Test
    void markFreeTransitionsBusyToFree() {
        TimeSlot slot = busySlot();
        slot.markFree();
        assertEquals(SlotStatus.FREE, slot.status());
        assertTrue(slot.isFree());
    }

    @Test
    void markFreeThrowsWhenFree() {
        TimeSlot slot = freeSlot();
        assertThrows(SlotNotModifiableException.class, slot::markFree);
    }

    @Test
    void markFreeThrowsWhenBooked() {
        TimeSlot slot = bookedSlot();
        assertThrows(SlotNotModifiableException.class, slot::markFree);
    }

    @Test
    void updateAllowedWhenFree() {
        TimeSlot slot = freeSlot();
        TimeRange newRange = new TimeRange(T2, T3);
        slot.update(newRange);
        assertEquals(newRange, slot.timeRange());
    }

    @Test
    void updateAllowedWhenBusy() {
        TimeSlot slot = busySlot();
        TimeRange newRange = new TimeRange(T2, T3);
        slot.update(newRange);
        assertEquals(newRange, slot.timeRange());
    }

    @Test
    void updateThrowsWhenBooked() {
        TimeSlot slot = bookedSlot();
        TimeRange newRange = new TimeRange(T2, T3);
        assertThrows(SlotNotModifiableException.class, () -> slot.update(newRange));
    }

    @Test
    void assertModifiableThrowsWhenBooked() {
        TimeSlot slot = bookedSlot();
        assertThrows(SlotNotModifiableException.class, slot::assertModifiable);
    }

    @Test
    void slotsAreEqualByIdentityOnly() {
        UUID uuid = UUID.randomUUID();
        UserId owner = new UserId(UUID.randomUUID());
        TimeRange range = new TimeRange(T1, T2);

        TimeSlot a = new TimeSlot(new SlotId(uuid), owner, range, SlotStatus.FREE);
        TimeSlot b = new TimeSlot(new SlotId(uuid), owner, range, SlotStatus.BUSY);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, freeSlot());
    }
}
