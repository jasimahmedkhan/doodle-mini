package com.minidoodle.schedular.meeting.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Meeting booking request")
public record BookMeetingRequest(
        @NotBlank @Size(max = 200) @Schema(example = "Roadmap planning") String title,
        @Schema(example = "Plan the next release") String description,
        @NotEmpty List<@Valid ParticipantRequest> participants
) {
}
