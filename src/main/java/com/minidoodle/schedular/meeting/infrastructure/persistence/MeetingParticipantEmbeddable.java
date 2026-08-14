package com.minidoodle.schedular.meeting.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MeetingParticipantEmbeddable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    protected MeetingParticipantEmbeddable() {
    }

    public MeetingParticipantEmbeddable(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
