package com.minidoodle.schedular.availability.api.dto;

import com.minidoodle.schedular.availability.domain.AvailabilityRange;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Merged availability range")
public record AvailabilityRangeResponse(
        Instant start,
        Instant end,
        @Schema(allowableValues = {"FREE", "BUSY", "BOOKED"}) String status
) {

    public static AvailabilityRangeResponse from(AvailabilityRange range) {
        return new AvailabilityRangeResponse(
                range.timeRange().start(),
                range.timeRange().end(),
                range.status().name()
        );
    }
}
