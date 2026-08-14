package com.minidoodle.schedular.meeting.application.usecase;

import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.meeting.domain.exception.MeetingNotFoundException;
import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Deletes a meeting and releases its slot atomically. */
@Service
@Transactional
public class CancelMeetingUseCase {

    private final MeetingRepository meetingRepository;
    private final SlotOperations slotOperations;

    public CancelMeetingUseCase(MeetingRepository meetingRepository, SlotOperations slotOperations) {
        this.meetingRepository = meetingRepository;
        this.slotOperations = slotOperations;
    }

    /** Removes the meeting before returning its BOOKED slot to FREE. */
    public void cancel(MeetingId meetingId) {
        Objects.requireNonNull(meetingId, "meetingId must not be null");
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found: " + meetingId));
        meetingRepository.delete(meetingId);
        slotOperations.release(meeting.slotId());
    }
}
