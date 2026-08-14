package com.minidoodle.schedular.meeting.application.usecase;

import com.minidoodle.schedular.meeting.application.exception.BookingConflictException;
import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.slot.application.operation.SlotOperations;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Books a meeting and reserves its slot in one transaction.
 * Database races are translated to a stable booking-conflict response for callers.
 */
@Service
@Transactional
public class BookMeetingUseCase {

    private final MeetingRepository meetingRepository;
    private final SlotOperations slotOperations;
    private final MeterRegistry meterRegistry;
    private final Timer bookingTimer;
    private final Counter bookingConflictCounter;

    public BookMeetingUseCase(
            MeetingRepository meetingRepository,
            SlotOperations slotOperations,
            MeterRegistry meterRegistry
    ) {
        this.meetingRepository = meetingRepository;
        this.slotOperations = slotOperations;
        this.meterRegistry = meterRegistry;
        this.bookingTimer = Timer.builder("minidoodle.meeting.booking.duration")
                .description("Time spent processing meeting booking attempts")
                .register(meterRegistry);
        this.bookingConflictCounter = Counter.builder("minidoodle.meeting.booking.conflicts")
                .description("Meeting bookings rejected by a concurrent database write")
                .register(meterRegistry);
    }

    /** Validates meeting details, reserves the slot, and persists the meeting. */
    public Meeting book(SlotId slotId, String title, String description, List<Participant> participants) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Meeting meeting = Meeting.create(slotId, title, description, participants);
            slotOperations.reserve(slotId);
            return meetingRepository.save(meeting);
        } catch (OptimisticLockingFailureException | DataIntegrityViolationException conflict) {
            // Domain failures such as a BUSY slot propagate unchanged; only database races map here.
            bookingConflictCounter.increment();
            throw new BookingConflictException("The slot was booked concurrently: " + slotId, conflict);
        } finally {
            sample.stop(bookingTimer);
        }
    }
}
