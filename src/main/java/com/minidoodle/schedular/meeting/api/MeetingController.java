package com.minidoodle.schedular.meeting.api;

import com.minidoodle.schedular.meeting.api.dto.BookMeetingRequest;
import com.minidoodle.schedular.meeting.api.dto.MeetingResponse;
import com.minidoodle.schedular.meeting.application.usecase.BookMeetingUseCase;
import com.minidoodle.schedular.meeting.application.usecase.CancelMeetingUseCase;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Meetings")
public class MeetingController {

    private final BookMeetingUseCase bookMeeting;
    private final CancelMeetingUseCase cancelMeeting;

    public MeetingController(BookMeetingUseCase bookMeeting, CancelMeetingUseCase cancelMeeting) {
        this.bookMeeting = bookMeeting;
        this.cancelMeeting = cancelMeeting;
    }

    @PostMapping("/slots/{slotId}/meetings")
    @Operation(summary = "Book a meeting in a free slot")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meeting booked"),
            @ApiResponse(responseCode = "404", description = "Slot not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slot unavailable or concurrent booking conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Invalid meeting",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MeetingResponse> book(
            @PathVariable UUID slotId,
            @Valid @RequestBody BookMeetingRequest request
    ) {
        MeetingResponse response = MeetingResponse.from(bookMeeting.book(
                new SlotId(slotId),
                request.title(),
                request.description(),
                request.participants().stream()
                        .map(participant -> new Participant(participant.name(), participant.email()))
                        .toList()
        ));
        return ResponseEntity.created(URI.create("/api/v1/meetings/" + response.id())).body(response);
    }

    @DeleteMapping("/meetings/{id}")
    @Operation(summary = "Cancel a meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meeting cancelled"),
            @ApiResponse(responseCode = "404", description = "Meeting not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelMeeting.cancel(new MeetingId(id));
        return ResponseEntity.noContent().build();
    }
}
