package com.minidoodle.schedular.meeting.domain;

import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;

import java.util.List;
import java.util.Objects;

/**
 * Meeting aggregate linked to a slot by SlotId only.
 * Participants are embedded contact details and are not required to be registered users.
 */
public class Meeting {

    private final MeetingId id;
    private final SlotId slotId;
    private final String title;
    private final String description;
    private final List<Participant> participants;

    public Meeting(MeetingId id, SlotId slotId, String title, String description, List<Participant> participants) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.slotId = Objects.requireNonNull(slotId, "slotId must not be null");
        this.title = validateTitle(title);
        this.description = description;
        this.participants = validateParticipants(participants);
    }

    public static Meeting create(SlotId slotId, String title, String description, List<Participant> participants) {
        return new Meeting(MeetingId.random(), slotId, title, description, participants);
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("title must not exceed 200 characters");
        }
        return title;
    }

    private static List<Participant> validateParticipants(List<Participant> participants) {
        Objects.requireNonNull(participants, "participants must not be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("at least one participant is required");
        }
        return List.copyOf(participants);
    }

    public MeetingId id() {
        return id;
    }

    public SlotId slotId() {
        return slotId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public List<Participant> participants() {
        return participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meeting meeting)) return false;
        return id.equals(meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Meeting[" + id + ", slot=" + slotId + "]";
    }
}
