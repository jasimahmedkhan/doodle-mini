package com.minidoodle.schedular.meeting.api.dto;

import com.minidoodle.schedular.meeting.application.ScheduledMeeting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A user's meeting and its scheduled slot range")
public record ScheduledMeetingResponse(
        UUID id,
        UUID slotId,
        Instant start,
        Instant end,
        String title,
        String description,
        List<ParticipantResponse> participants
) {

    public ScheduledMeetingResponse {
        participants = List.copyOf(participants);
    }

    public static ScheduledMeetingResponse from(ScheduledMeeting scheduled) {
        var meeting = scheduled.meeting();
        return new ScheduledMeetingResponse(
                meeting.id().value(),
                meeting.slotId().value(),
                scheduled.timeRange().start(),
                scheduled.timeRange().end(),
                meeting.title(),
                meeting.description(),
                meeting.participants().stream().map(ParticipantResponse::from).toList()
        );
    }
}
