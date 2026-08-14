package com.minidoodle.schedular.slot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Slot creation request")
public record CreateSlotRequest(
        @NotNull UUID ownerId,
        @NotNull @Schema(type = "string", format = "date-time", example = "2026-08-14T09:00:00Z") Instant start,
        @NotNull @Schema(type = "string", format = "date-time", example = "2026-08-14T10:00:00Z") Instant end
) {

    @AssertTrue(message = "start must be before end")
    @Schema(hidden = true)
    public boolean isRangeValid() {
        return start == null || end == null || start.isBefore(end);
    }
}
