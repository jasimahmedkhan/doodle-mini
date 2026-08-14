package com.minidoodle.schedular.meeting.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meeting")
public class MeetingEntity {

    @Id
    private UUID id;

    // This unique foreign key is the only persisted meeting-to-slot link.
    @Column(name = "slot_id", nullable = false, unique = true)
    private UUID slotId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participant", joinColumns = @JoinColumn(name = "meeting_id"))
    private List<MeetingParticipantEmbeddable> participants = new ArrayList<>();

    protected MeetingEntity() {
    }

    public MeetingEntity(
            UUID id,
            UUID slotId,
            String title,
            String description,
            List<MeetingParticipantEmbeddable> participants
    ) {
        this.id = id;
        this.slotId = slotId;
        this.title = title;
        this.description = description;
        this.participants = new ArrayList<>(participants);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<MeetingParticipantEmbeddable> getParticipants() {
        return participants;
    }
}
