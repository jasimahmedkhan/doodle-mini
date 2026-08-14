package com.minidoodle.schedular.slot.api;

import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.api.exception.InvalidRequestException;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.api.dto.SlotResponse;
import com.minidoodle.schedular.slot.application.usecase.GetUserSlotsUseCase;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/slots")
@Tag(name = "Slots")
public class UserSlotController {

    private final GetUserSlotsUseCase getUserSlots;
    private final GetUserUseCase getUser;

    public UserSlotController(GetUserSlotsUseCase getUserSlots, GetUserUseCase getUser) {
        this.getUserSlots = getUserSlots;
        this.getUser = getUser;
    }

    @GetMapping
    @Operation(summary = "List a user's individual slots within a time window")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slots returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SlotResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid window",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<SlotResponse> get(
            @PathVariable UUID userId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(example = "2030-01-01T00:00:00Z") Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(example = "2030-02-01T00:00:00Z") Instant to
    ) {
        if (!from.isBefore(to)) {
            throw new InvalidRequestException("from must be before to");
        }

        UserId owner = new UserId(userId);
        getUser.get(owner);
        return getUserSlots.get(owner, new TimeRange(from, to)).stream()
                .map(SlotResponse::from)
                .toList();
    }
}
