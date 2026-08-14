package com.minidoodle.schedular.slot.domain;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;

import java.util.List;
import java.util.Optional;

/** Persistence port owned by the slot domain. Other modules use slot application contracts instead. */
public interface SlotRepository {

    Optional<TimeSlot> findById(SlotId id);

    TimeSlot save(TimeSlot slot);

    void delete(SlotId id);

    boolean existsOverlapping(UserId owner, TimeRange timeRange);

    List<TimeSlot> findByOwnerAndOverlapping(UserId owner, TimeRange timeRange);
}
