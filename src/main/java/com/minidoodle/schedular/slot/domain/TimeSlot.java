package com.minidoodle.schedular.slot.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.exception.SlotNotBookableException;
import com.minidoodle.schedular.slot.domain.exception.SlotNotModifiableException;

import java.util.Objects;

/**
 * Slot aggregate root and owner of the FREE/BUSY/BOOKED state machine.
 * Meeting linkage deliberately lives outside this aggregate; booking is represented only by status.
 */
public class TimeSlot {

    private final SlotId id;
    private final UserId owner;
    private TimeRange timeRange;
    private SlotStatus status;
    private final long version;

    public TimeSlot(SlotId id, UserId owner, TimeRange timeRange, SlotStatus status) {
        this(id, owner, timeRange, status, 0L);
    }

    public TimeSlot(SlotId id, UserId owner, TimeRange timeRange, SlotStatus status, long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
        this.timeRange = Objects.requireNonNull(timeRange, "timeRange must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.version = version;
    }

    public SlotId id() {
        return id;
    }

    public UserId owner() {
        return owner;
    }

    public TimeRange timeRange() {
        return timeRange;
    }

    public SlotStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public boolean isFree() {
        return status == SlotStatus.FREE;
    }

    public boolean isBusy() {
        return status == SlotStatus.BUSY;
    }

    public boolean isBooked() {
        return status == SlotStatus.BOOKED;
    }

    public void book() {
        if (status != SlotStatus.FREE) {
            throw new SlotNotBookableException("Only FREE slots can be booked. Current status: " + status);
        }
        this.status = SlotStatus.BOOKED;
    }

    public void cancelBooking() {
        if (status != SlotStatus.BOOKED) {
            throw new SlotNotBookableException("Only BOOKED slots can have their booking cancelled. Current status: " + status);
        }
        this.status = SlotStatus.FREE;
    }

    public void markBusy() {
        if (status != SlotStatus.FREE) {
            throw new SlotNotModifiableException("Only FREE slots can be marked busy. Current status: " + status);
        }
        this.status = SlotStatus.BUSY;
    }

    public void markFree() {
        if (status != SlotStatus.BUSY) {
            throw new SlotNotModifiableException("Only BUSY slots can be marked free. Current status: " + status);
        }
        this.status = SlotStatus.FREE;
    }

    public void update(TimeRange newTimeRange) {
        assertModifiable();
        this.timeRange = Objects.requireNonNull(newTimeRange, "newTimeRange must not be null");
    }

    public void assertModifiable() {
        if (status == SlotStatus.BOOKED) {
            // The meeting must be cancelled first so the slot and meeting cannot disagree.
            throw new SlotNotModifiableException("BOOKED slots cannot be modified or deleted until the meeting is cancelled");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return id.equals(timeSlot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TimeSlot[" + id + ", " + owner + ", " + timeRange + ", " + status + "]";
    }
}
