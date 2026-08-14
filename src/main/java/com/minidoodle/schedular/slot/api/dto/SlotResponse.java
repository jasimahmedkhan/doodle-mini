package com.minidoodle.schedular.slot.api.dto;

import com.minidoodle.schedular.slot.domain.TimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Time slot")
public record SlotResponse(
        UUID id,
        UUID ownerId,
        Instant start,
        Instant end,
        @Schema(allowableValues = {"FREE", "BUSY", "BOOKED"})
        String status,
        long version
) {

    public static SlotResponse from(TimeSlot slot) {
        return new SlotResponse(
                slot.id().value(),
                slot.owner().value(),
                slot.timeRange().start(),
                slot.timeRange().end(),
                slot.status().name(),
                slot.version()
        );
    }
}
