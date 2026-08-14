package com.minidoodle.schedular.infrastructure;

import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.SlotStatus;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("minidoodle")
            .withUsername("minidoodle")
            .withPassword("minidoodle");

    private static final Instant T09 = instant("09:00");
    private static final Instant T10 = instant("10:00");
    private static final Instant T11 = instant("11:00");
    private static final Instant T12 = instant("12:00");
    private static final Instant T13 = instant("13:00");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private UserRepository users;

    @Autowired
    private SlotRepository slots;

    @Autowired
    private MeetingRepository meetings;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE meeting_participant, meeting, time_slot, users CASCADE");
    }

    @Test
    void adaptersRoundTripTheirAggregatesAndDeleteSupportedAggregates() {
        User user = users.save(new User(UserId.random(), "Alice", "alice@example.com"));
        assertTrue(users.existsById(user.id()));
        assertTrue(users.existsByEmail(user.email()));
        assertEquals(user, users.findById(user.id()).orElseThrow());

        TimeSlot slot = slots.save(slot(user.id(), T09, T10, SlotStatus.BOOKED));
        TimeSlot reloadedSlot = slots.findById(slot.id()).orElseThrow();
        assertEquals(slot.id(), reloadedSlot.id());
        assertEquals(slot.owner(), reloadedSlot.owner());
        assertEquals(slot.timeRange(), reloadedSlot.timeRange());
        assertEquals(slot.status(), reloadedSlot.status());

        Meeting meeting = meetings.save(Meeting.create(
                slot.id(),
                "Planning",
                "Roadmap",
                List.of(
                        new Participant("Alice", "alice@example.com"),
                        new Participant("Bob", "bob@example.com")
                )
        ));
        Meeting reloadedMeeting = meetings.findById(meeting.id()).orElseThrow();
        assertEquals(meeting.id(), reloadedMeeting.id());
        assertEquals(meeting.slotId(), reloadedMeeting.slotId());
        assertEquals(meeting.title(), reloadedMeeting.title());
        assertEquals(meeting.description(), reloadedMeeting.description());
        assertEquals(Set.copyOf(meeting.participants()), Set.copyOf(reloadedMeeting.participants()));
        assertEquals(meeting, meetings.findBySlotId(slot.id()).orElseThrow());

        meetings.delete(meeting.id());
        assertFalse(meetings.findById(meeting.id()).isPresent());
        slots.delete(slot.id());
        assertFalse(slots.findById(slot.id()).isPresent());
    }

    @Test
    void exclusionConstraintRejectsOverlappingSlots() {
        User user = users.save(new User(UserId.random(), "Alice", "alice@example.com"));
        slots.save(slot(user.id(), T09, T11, SlotStatus.FREE));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> slots.save(slot(user.id(), T10, T12, SlotStatus.FREE))
        );
    }

    @Test
    void overlapQueryReturnsExactlyIntersectingSlots() {
        User user = users.save(new User(UserId.random(), "Alice", "alice@example.com"));
        TimeSlot first = slots.save(slot(user.id(), T09, T10, SlotStatus.FREE));
        TimeSlot second = slots.save(slot(user.id(), T10, T11, SlotStatus.BUSY));
        slots.save(slot(user.id(), T12, T13, SlotStatus.FREE));

        TimeRange window = new TimeRange(instant("09:30"), instant("10:30"));

        assertTrue(slots.existsOverlapping(user.id(), window));
        assertEquals(
                List.of(first.id(), second.id()),
                slots.findByOwnerAndOverlapping(user.id(), window).stream().map(TimeSlot::id).toList()
        );
    }

    @Test
    void versionIncrementsAndStaleWritesFail() {
        User user = users.save(new User(UserId.random(), "Alice", "alice@example.com"));
        TimeSlot inserted = slots.save(slot(user.id(), T09, T10, SlotStatus.FREE));
        TimeSlot firstWriter = slots.findById(inserted.id()).orElseThrow();
        TimeSlot staleWriter = slots.findById(inserted.id()).orElseThrow();

        firstWriter.markBusy();
        TimeSlot updated = slots.save(firstWriter);

        assertEquals(inserted.version() + 1, updated.version());
        staleWriter.markBusy();
        assertThrows(OptimisticLockingFailureException.class, () -> slots.save(staleWriter));
    }

    @Test
    void uniqueMeetingSlotConstraintRejectsASecondMeeting() {
        User user = users.save(new User(UserId.random(), "Alice", "alice@example.com"));
        TimeSlot slot = slots.save(slot(user.id(), T09, T10, SlotStatus.BOOKED));
        meetings.save(Meeting.create(
                slot.id(),
                "First",
                null,
                List.of(new Participant("Alice", "alice@example.com"))
        ));

        Meeting second = Meeting.create(
                slot.id(),
                "Second",
                null,
                List.of(new Participant("Bob", "bob@example.com"))
        );

        assertThrows(DataIntegrityViolationException.class, () -> meetings.save(second));
    }

    private static TimeSlot slot(UserId ownerId, Instant start, Instant end, SlotStatus status) {
        return new TimeSlot(SlotId.random(), ownerId, new TimeRange(start, end), status);
    }

    private static Instant instant(String time) {
        return Instant.parse("2026-08-14T" + time + ":00Z");
    }
}
