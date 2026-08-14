package com.minidoodle.schedular.slot.application.support;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FakeSlotRepository implements SlotRepository {

    private final Map<SlotId, TimeSlot> slots = new LinkedHashMap<>();

    @Override
    public Optional<TimeSlot> findById(SlotId id) {
        return Optional.ofNullable(slots.get(id));
    }

    @Override
    public TimeSlot save(TimeSlot slot) {
        slots.put(slot.id(), slot);
        return slot;
    }

    @Override
    public void delete(SlotId id) {
        slots.remove(id);
    }

    @Override
    public boolean existsOverlapping(UserId owner, TimeRange timeRange) {
        return slots.values().stream()
                .anyMatch(slot -> slot.owner().equals(owner) && slot.timeRange().overlaps(timeRange));
    }

    @Override
    public List<TimeSlot> findByOwnerAndOverlapping(UserId owner, TimeRange timeRange) {
        return slots.values().stream()
                .filter(slot -> slot.owner().equals(owner))
                .filter(slot -> slot.timeRange().overlaps(timeRange))
                .toList();
    }
}
