package com.minidoodle.schedular.availability.api.dto;

import com.minidoodle.schedular.availability.domain.Availability;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Merged availability within the requested window")
public record AvailabilityResponse(
        UUID userId,
        Instant from,
        Instant to,
        List<AvailabilityRangeResponse> ranges
) {

    public AvailabilityResponse {
        ranges = List.copyOf(ranges);
    }

    public static AvailabilityResponse from(UUID userId, Instant from, Instant to, Availability availability) {
        return new AvailabilityResponse(
                userId,
                from,
                to,
                availability.ranges().stream().map(AvailabilityRangeResponse::from).toList()
        );
    }
}
