package com.minidoodle.schedular.user.api.dto;

import com.minidoodle.schedular.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Registered user")
public record UserResponse(UUID id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id().value(), user.name(), user.email());
    }
}
