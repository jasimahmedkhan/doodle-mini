package com.minidoodle.schedular.slot.application;

import com.minidoodle.schedular.shared.domain.UserId;

import java.time.Instant;
import java.util.List;

/** Public read contract for time-window consumers without leaking slot-domain objects. */
public interface SlotAvailabilityQuery {

    /** Returns every slot intersecting the requested window; merging belongs to availability. */
    List<SlotView> findSlots(UserId userId, Instant from, Instant to);
}
