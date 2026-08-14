package com.minidoodle.schedular.meeting.domain;

import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeetingTest {

    private static final MeetingId MEETING_ID = MeetingId.random();
    private static final SlotId SLOT_ID = SlotId.random();
    private static final Participant ALICE = new Participant("Alice", "alice@example.com");
    private static final Participant BOB = new Participant("Bob", "bob@example.com");

    @Test
    void createsMeetingWithRequiredFields() {
        Meeting meeting = new Meeting(MEETING_ID, SLOT_ID, "Stand-up", null, List.of(ALICE));

        assertEquals(MEETING_ID, meeting.id());
        assertEquals(SLOT_ID, meeting.slotId());
        assertEquals("Stand-up", meeting.title());
        assertNull(meeting.description());
        assertEquals(List.of(ALICE), meeting.participants());
    }

    @Test
    void createsMeetingWithDescription() {
        Meeting meeting = new Meeting(MEETING_ID, SLOT_ID, "Planning", "Q4 goals", List.of(ALICE, BOB));

        assertEquals("Planning", meeting.title());
        assertEquals("Q4 goals", meeting.description());
        assertEquals(List.of(ALICE, BOB), meeting.participants());
    }

    @Test
    void rejectsNullId() {
        assertThrows(NullPointerException.class,
                () -> new Meeting(null, SLOT_ID, "Title", null, List.of(ALICE)));
    }

    @Test
    void rejectsNullSlotId() {
        assertThrows(NullPointerException.class,
                () -> new Meeting(MEETING_ID, null, "Title", null, List.of(ALICE)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingTitle(String title) {
        assertThrows(IllegalArgumentException.class,
                () -> new Meeting(MEETING_ID, SLOT_ID, title, null, List.of(ALICE)));
    }

    @Test
    void rejectsTitleExceeding200Characters() {
        String longTitle = "x".repeat(201);
        assertThrows(IllegalArgumentException.class,
                () -> new Meeting(MEETING_ID, SLOT_ID, longTitle, null, List.of(ALICE)));
    }

    @Test
    void acceptsTitleOfExactly200Characters() {
        String title = "x".repeat(200);
        assertDoesNotThrow(() -> new Meeting(MEETING_ID, SLOT_ID, title, null, List.of(ALICE)));
    }

    @Test
    void rejectsNullParticipants() {
        assertThrows(NullPointerException.class,
                () -> new Meeting(MEETING_ID, SLOT_ID, "Title", null, null));
    }

    @Test
    void rejectsEmptyParticipants() {
        assertThrows(IllegalArgumentException.class,
                () -> new Meeting(MEETING_ID, SLOT_ID, "Title", null, Collections.emptyList()));
    }

    @Test
    void participantsListIsImmutable() {
        Meeting meeting = new Meeting(MEETING_ID, SLOT_ID, "Title", null, List.of(ALICE));
        assertThrows(UnsupportedOperationException.class,
                () -> meeting.participants().add(BOB));
    }

    @Test
    void equalsAndHashCodeByIdOnly() {
        Meeting a = new Meeting(MEETING_ID, SLOT_ID, "A", null, List.of(ALICE));
        Meeting b = new Meeting(MEETING_ID, SlotId.random(), "B", "desc", List.of(ALICE, BOB));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Meeting(MeetingId.random(), SLOT_ID, "A", null, List.of(ALICE)));
    }
}
