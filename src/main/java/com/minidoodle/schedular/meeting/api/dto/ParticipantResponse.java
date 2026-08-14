package com.minidoodle.schedular.meeting.api.dto;

import com.minidoodle.schedular.meeting.domain.Participant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Meeting participant")
public record ParticipantResponse(String name, String email) {

    public static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(participant.name(), participant.email());
    }
}
