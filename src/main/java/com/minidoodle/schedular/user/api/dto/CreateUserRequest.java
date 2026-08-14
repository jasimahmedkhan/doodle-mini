package com.minidoodle.schedular.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User registration request")
public record CreateUserRequest(
        @NotBlank @Schema(example = "Alice") String name,
        @NotBlank @Email @Schema(example = "alice@example.com") String email
) {
}
