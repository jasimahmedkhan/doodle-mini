package com.minidoodle.schedular.meeting.domain;

import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;

import java.util.Optional;

public interface MeetingRepository {

    Optional<Meeting> findById(MeetingId id);

    Meeting save(Meeting meeting);

    void delete(MeetingId id);

    Optional<Meeting> findBySlotId(SlotId slotId);
}
