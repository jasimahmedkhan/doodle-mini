package com.minidoodle.schedular.meeting.api;

import com.minidoodle.schedular.meeting.api.dto.ScheduledMeetingResponse;
import com.minidoodle.schedular.meeting.application.usecase.GetUserMeetingsUseCase;
import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.api.exception.InvalidRequestException;
import com.minidoodle.schedular.shared.domain.UserId;
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
@RequestMapping("/api/v1/users/{userId}/meetings")
@Tag(name = "Meetings")
public class UserMeetingController {

    private final GetUserMeetingsUseCase getUserMeetings;
    private final GetUserUseCase getUser;

    public UserMeetingController(GetUserMeetingsUseCase getUserMeetings, GetUserUseCase getUser) {
        this.getUserMeetings = getUserMeetings;
        this.getUser = getUser;
    }

    @GetMapping
    @Operation(summary = "List a user's meetings within a time window")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meetings returned",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ScheduledMeetingResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid window",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ScheduledMeetingResponse> get(
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
        return getUserMeetings.get(owner, from, to).stream()
                .map(ScheduledMeetingResponse::from)
                .toList();
    }
}
