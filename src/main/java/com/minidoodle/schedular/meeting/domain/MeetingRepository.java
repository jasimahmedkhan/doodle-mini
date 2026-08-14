package com.minidoodle.schedular.meeting.domain;

import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Persistence port owned by the meeting domain, not a cross-module API. */
public interface MeetingRepository {

    Optional<Meeting> findById(MeetingId id);

    Meeting save(Meeting meeting);

    void delete(MeetingId id);

    Optional<Meeting> findBySlotId(SlotId slotId);

    List<Meeting> findBySlotIds(Collection<SlotId> slotIds);
}
