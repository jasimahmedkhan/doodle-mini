package com.minidoodle.schedular.meeting.application.support;

import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class FakeMeetingRepository implements MeetingRepository {

    private final Map<MeetingId, Meeting> meetings = new HashMap<>();
    private RuntimeException saveFailure;

    @Override
    public Optional<Meeting> findById(MeetingId id) {
        return Optional.ofNullable(meetings.get(id));
    }

    @Override
    public Meeting save(Meeting meeting) {
        if (saveFailure != null) {
            throw saveFailure;
        }
        meetings.put(meeting.id(), meeting);
        return meeting;
    }

    @Override
    public void delete(MeetingId id) {
        meetings.remove(id);
    }

    @Override
    public Optional<Meeting> findBySlotId(SlotId slotId) {
        return meetings.values().stream()
                .filter(meeting -> meeting.slotId().equals(slotId))
                .findFirst();
    }

    public void failOnSave(RuntimeException failure) {
        this.saveFailure = failure;
    }

    public boolean isEmpty() {
        return meetings.isEmpty();
    }
}
