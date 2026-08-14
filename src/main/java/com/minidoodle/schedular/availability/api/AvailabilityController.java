package com.minidoodle.schedular.availability.api;

import com.minidoodle.schedular.availability.api.dto.AvailabilityResponse;
import com.minidoodle.schedular.availability.application.usecase.GetAvailabilityUseCase;
import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.api.exception.InvalidRequestException;
import com.minidoodle.schedular.shared.domain.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/availability")
@Tag(name = "Availability")
public class AvailabilityController {

    private final GetAvailabilityUseCase getAvailability;

    public AvailabilityController(GetAvailabilityUseCase getAvailability) {
        this.getAvailability = getAvailability;
    }

    @GetMapping
    @Operation(summary = "Get merged availability for a full window")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability returned"),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid window",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AvailabilityResponse get(
            @PathVariable UUID userId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(example = "2026-08-14T09:00:00Z") Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(example = "2026-08-14T17:00:00Z") Instant to
    ) {
        if (!from.isBefore(to)) {
            throw new InvalidRequestException("from must be before to");
        }
        return AvailabilityResponse.from(
                userId,
                from,
                to,
                getAvailability.get(new UserId(userId), from, to)
        );
    }
}
