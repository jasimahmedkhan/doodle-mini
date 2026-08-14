package com.minidoodle.schedular.slot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Slot time-range update")
public record UpdateSlotRequest(
        @NotNull @Schema(type = "string", format = "date-time", example = "2026-08-14T09:00:00Z") Instant start,
        @NotNull @Schema(type = "string", format = "date-time", example = "2026-08-14T10:00:00Z") Instant end
) {

    @AssertTrue(message = "start must be before end")
    @Schema(hidden = true)
    public boolean isRangeValid() {
        return start == null || end == null || start.isBefore(end);
    }
}
