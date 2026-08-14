package com.minidoodle.schedular.meeting.application.usecase;

import com.minidoodle.schedular.meeting.application.exception.BookingConflictException;
import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class BookMeetingUseCase {

    private final MeetingRepository meetingRepository;
    private final SlotOperations slotOperations;

    public BookMeetingUseCase(
            MeetingRepository meetingRepository,
            SlotOperations slotOperations
    ) {
        this.meetingRepository = meetingRepository;
        this.slotOperations = slotOperations;

    }

    public Meeting book(SlotId slotId, String title, String description, List<Participant> participants) {
        try {
            Meeting meeting = Meeting.create(slotId, title, description, participants);
            slotOperations.reserve(slotId);
            return meetingRepository.save(meeting);
        } catch (OptimisticLockingFailureException | DataIntegrityViolationException conflict) {
            // Domain failures such as a BUSY slot propagate unchanged; only database races map here.
            throw new BookingConflictException("The slot was booked concurrently: " + slotId, conflict);
        } finally {

        }
    }
}
