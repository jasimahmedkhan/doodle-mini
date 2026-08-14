package com.minidoodle.schedular.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Consistent API error payload")
public record ErrorResponse(
        @Schema(example = "VALIDATION_ERROR") String code,
        @Schema(example = "title: must not be blank") String message,
        Instant timestamp,
        @Schema(example = "/api/v1/slots") String path
) {
}
