package com.minidoodle.schedular.meeting.application;

import com.minidoodle.schedular.meeting.application.exception.BookingConflictException;
import com.minidoodle.schedular.meeting.application.support.FailingSlotOperations;
import com.minidoodle.schedular.meeting.application.support.FakeMeetingRepository;
import com.minidoodle.schedular.meeting.application.support.InMemorySlotOperations;
import com.minidoodle.schedular.meeting.application.usecase.BookMeetingUseCase;
import com.minidoodle.schedular.meeting.application.usecase.CancelMeetingUseCase;
import com.minidoodle.schedular.meeting.application.usecase.GetUserMeetingsUseCase;
import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.application.SlotAvailabilityQuery;
import com.minidoodle.schedular.slot.application.SlotView;
import com.minidoodle.schedular.slot.domain.SlotStatus;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.slot.domain.exception.SlotNotBookableException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MeetingApplicationTest {

    private static final Participant ALICE = new Participant("Alice", "alice@example.com");

    @Test
    void booksMeetingAndReservesFreeSlot() {
        TimeSlot slot = slot(SlotStatus.FREE);
        FakeMeetingRepository meetings = new FakeMeetingRepository();
        InMemorySlotOperations slots = new InMemorySlotOperations(slot);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        BookMeetingUseCase useCase = new BookMeetingUseCase(meetings, slots, metrics);

        Meeting meeting = useCase.book(slot.id(), "Planning", "Roadmap", List.of(ALICE));

        assertEquals(slot.id(), meeting.slotId());
        assertEquals(Optional.of(meeting), meetings.findById(meeting.id()));
        assertTrue(slot.isBooked());
        assertEquals(1, metrics.get("minidoodle.meeting.booking.duration").timer().count());
    }

    @Test
    void propagatesNotBookableWhenSlotIsNotFree() {
        TimeSlot slot = slot(SlotStatus.BUSY);
        FakeMeetingRepository meetings = new FakeMeetingRepository();
        BookMeetingUseCase useCase = new BookMeetingUseCase(
                meetings,
                new InMemorySlotOperations(slot),
                new SimpleMeterRegistry()
        );

        assertThrows(
                SlotNotBookableException.class,
                () -> useCase.book(slot.id(), "Planning", null, List.of(ALICE))
        );
        assertTrue(meetings.isEmpty());
    }

    @Test
    void cancellationDeletesMeetingAndReleasesSlot() {
        TimeSlot slot = slot(SlotStatus.BOOKED);
        Meeting meeting = Meeting.create(slot.id(), "Planning", null, List.of(ALICE));
        FakeMeetingRepository meetings = new FakeMeetingRepository();
        meetings.save(meeting);
        CancelMeetingUseCase useCase = new CancelMeetingUseCase(meetings, new InMemorySlotOperations(slot));

        useCase.cancel(meeting.id());

        assertFalse(meetings.findById(meeting.id()).isPresent());
        assertTrue(slot.isFree());
    }

    @Test
    void mapsUniqueConstraintFailureToBookingConflict() {
        TimeSlot slot = slot(SlotStatus.FREE);
        FakeMeetingRepository meetings = new FakeMeetingRepository();
        meetings.failOnSave(new DataIntegrityViolationException("meeting.slot_id is unique"));
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        BookMeetingUseCase useCase = new BookMeetingUseCase(
                meetings,
                new InMemorySlotOperations(slot),
                metrics
        );

        BookingConflictException conflict = assertThrows(
                BookingConflictException.class,
                () -> useCase.book(slot.id(), "Planning", null, List.of(ALICE))
        );

        assertInstanceOf(DataIntegrityViolationException.class, conflict.getCause());
        assertEquals(1, metrics.get("minidoodle.meeting.booking.conflicts").counter().count());
        assertEquals(1, metrics.get("minidoodle.meeting.booking.duration").timer().count());
    }

    @Test
    void mapsOptimisticLockFailureToBookingConflict() {
        var slots = new FailingSlotOperations(new OptimisticLockingFailureException("version changed"));
        BookMeetingUseCase useCase = new BookMeetingUseCase(
                new FakeMeetingRepository(),
                slots,
                new SimpleMeterRegistry()
        );

        BookingConflictException conflict = assertThrows(
                BookingConflictException.class,
                () -> useCase.book(SlotId.random(), "Planning", null, List.of(ALICE))
        );

        assertInstanceOf(OptimisticLockingFailureException.class, conflict.getCause());
    }

    @Test
    void listsMeetingsForTheUsersWindowInStartTimeOrder() {
        UserId userId = UserId.random();
        TimeRange earlyRange = range("2026-08-14T09:00:00Z", "2026-08-14T10:00:00Z");
        TimeRange lateRange = range("2026-08-14T11:00:00Z", "2026-08-14T12:00:00Z");
        SlotId earlySlot = SlotId.random();
        SlotId lateSlot = SlotId.random();

        Meeting earlyMeeting = Meeting.create(earlySlot, "Early", null, List.of(ALICE));
        Meeting lateMeeting = Meeting.create(lateSlot, "Late", null, List.of(ALICE));
        Meeting outsideWindow = Meeting.create(SlotId.random(), "Outside", null, List.of(ALICE));
        FakeMeetingRepository meetings = new FakeMeetingRepository();
        meetings.save(lateMeeting);
        meetings.save(outsideWindow);
        meetings.save(earlyMeeting);

        SlotAvailabilityQuery slots = (owner, from, to) -> List.of(
                new SlotView(lateSlot, lateRange, SlotView.Status.BOOKED),
                new SlotView(earlySlot, earlyRange, SlotView.Status.BOOKED)
        );
        GetUserMeetingsUseCase useCase = new GetUserMeetingsUseCase(meetings, slots);

        List<ScheduledMeeting> result = useCase.get(
                userId,
                Instant.parse("2026-08-14T08:00:00Z"),
                Instant.parse("2026-08-14T13:00:00Z")
        );

        assertEquals(List.of(earlyMeeting, lateMeeting), result.stream().map(ScheduledMeeting::meeting).toList());
        assertEquals(List.of(earlyRange, lateRange), result.stream().map(ScheduledMeeting::timeRange).toList());
    }

    private static TimeSlot slot(SlotStatus status) {
        return new TimeSlot(
                SlotId.random(),
                UserId.random(),
                new TimeRange(
                        Instant.parse("2026-08-14T10:00:00Z"),
                        Instant.parse("2026-08-14T11:00:00Z")
                ),
                status
        );
    }

    private static TimeRange range(String start, String end) {
        return new TimeRange(Instant.parse(start), Instant.parse(end));
    }

}
