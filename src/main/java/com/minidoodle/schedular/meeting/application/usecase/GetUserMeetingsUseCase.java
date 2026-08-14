package com.minidoodle.schedular.meeting.application.usecase;

import com.minidoodle.schedular.meeting.application.ScheduledMeeting;
import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.SlotAvailabilityQuery;
import com.minidoodle.schedular.slot.application.SlotView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Lists meetings through the slot module's public read contract. */
@Service
@Transactional(readOnly = true)
public class GetUserMeetingsUseCase {

    private static final Comparator<ScheduledMeeting> BY_START_AND_ID = Comparator
            .comparing((ScheduledMeeting scheduled) -> scheduled.timeRange().start())
            .thenComparing(scheduled -> scheduled.meeting().id().value());

    private final MeetingRepository meetingRepository;
    private final SlotAvailabilityQuery slotQuery;

    public GetUserMeetingsUseCase(MeetingRepository meetingRepository, SlotAvailabilityQuery slotQuery) {
        this.meetingRepository = meetingRepository;
        this.slotQuery = slotQuery;
    }

    /** Returns meetings whose owning slots intersect the requested window. */
    public List<ScheduledMeeting> get(UserId userId, Instant from, Instant to) {
        Map<SlotId, SlotView> slotsById = slotQuery.findSlots(userId, from, to).stream()
                .collect(Collectors.toMap(SlotView::slotId, Function.identity()));

        return meetingRepository.findBySlotIds(slotsById.keySet()).stream()
                .map(meeting -> schedule(meeting, slotsById))
                .sorted(BY_START_AND_ID)
                .toList();
    }

    private static ScheduledMeeting schedule(Meeting meeting, Map<SlotId, SlotView> slotsById) {
        return new ScheduledMeeting(meeting, slotsById.get(meeting.slotId()).timeRange());
    }
}
