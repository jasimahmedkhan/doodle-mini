package com.minidoodle.schedular.slot.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.exception.SlotNotBookableException;

import java.util.Objects;


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
