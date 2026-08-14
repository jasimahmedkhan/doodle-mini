package com.minidoodle.schedular.meeting.api.dto;

import com.minidoodle.schedular.meeting.domain.Meeting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Booked meeting")
public record MeetingResponse(
        UUID id,
        UUID slotId,
        String title,
        String description,
        List<ParticipantResponse> participants
) {

    public MeetingResponse {
        participants = List.copyOf(participants);
    }

    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(
                meeting.id().value(),
                meeting.slotId().value(),
                meeting.title(),
                meeting.description(),
                meeting.participants().stream().map(ParticipantResponse::from).toList()
        );
    }
}
