package com.minidoodle.schedular.meeting.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Meeting participant")
public record ParticipantRequest(
        @NotBlank @Schema(example = "Alice") String name,
        @NotBlank @Email @Schema(example = "alice@example.com") String email
) {
}
