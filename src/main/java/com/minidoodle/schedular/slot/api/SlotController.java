package com.minidoodle.schedular.slot.api;

import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.api.dto.CreateSlotRequest;
import com.minidoodle.schedular.slot.api.dto.SlotResponse;
import com.minidoodle.schedular.slot.api.dto.UpdateSlotRequest;
import com.minidoodle.schedular.slot.application.usecase.*;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
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
@RequestMapping("/api/v1/slots")
@Tag(name = "Slots")
public class SlotController {

    private final CreateSlotUseCase createSlot;
    private final GetSlotUseCase getSlot;
    private final UpdateSlotUseCase updateSlot;
    private final DeleteSlotUseCase deleteSlot;
    private final MarkSlotBusyUseCase markSlotBusy;
    private final MarkSlotFreeUseCase markSlotFree;
    private final GetUserUseCase getUser;

    public SlotController(
            CreateSlotUseCase createSlot,
            GetSlotUseCase getSlot,
            UpdateSlotUseCase updateSlot,
            DeleteSlotUseCase deleteSlot,
            MarkSlotBusyUseCase markSlotBusy,
            MarkSlotFreeUseCase markSlotFree,
            GetUserUseCase getUser
    ) {
        this.createSlot = createSlot;
        this.getSlot = getSlot;
        this.updateSlot = updateSlot;
        this.deleteSlot = deleteSlot;
        this.markSlotBusy = markSlotBusy;
        this.markSlotFree = markSlotFree;
        this.getUser = getUser;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get info about a slot")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot returned",
                    content = @Content(schema = @Schema(implementation = SlotResponse.class))),
            @ApiResponse(responseCode = "404", description = "Slot not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public SlotResponse get(@PathVariable UUID id) {
        return SlotResponse.from(getSlot.get(new SlotId(id)));
    }

    @PostMapping
    @Operation(summary = "Create a free slot")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Slot created"),
            @ApiResponse(responseCode = "404", description = "Owner not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Invalid or overlapping slot",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SlotResponse> create(@Valid @RequestBody CreateSlotRequest request) {
        UserId ownerId = new UserId(request.ownerId());
        getUser.get(ownerId);
        SlotResponse response = SlotResponse.from(createSlot.create(
                ownerId,
                new TimeRange(request.start(), request.end())
        ));
        return ResponseEntity.created(URI.create("/api/v1/slots/" + response.id())).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a slot time range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot updated"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Booked slot cannot be updated"),
            @ApiResponse(responseCode = "422", description = "Invalid or overlapping slot")
    })
    public SlotResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSlotRequest request) {
        return SlotResponse.from(updateSlot.update(
                new SlotId(id),
                new TimeRange(request.start(), request.end())
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a slot")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Slot deleted"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Booked slot cannot be deleted")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteSlot.delete(new SlotId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/busy")
    @Operation(summary = "Mark a free slot busy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot marked busy"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Invalid slot transition")
    })
    public SlotResponse markBusy(@PathVariable UUID id) {
        return SlotResponse.from(markSlotBusy.markBusy(new SlotId(id)));
    }

    @PostMapping("/{id}/free")
    @Operation(summary = "Mark a busy slot free")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot marked free"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Invalid slot transition")
    })
    public SlotResponse markFree(@PathVariable UUID id) {
        return SlotResponse.from(markSlotFree.markFree(new SlotId(id)));
    }
}
