package com.minidoodle.schedular.user.api;

import com.minidoodle.schedular.shared.api.ErrorResponse;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.api.dto.CreateUserRequest;
import com.minidoodle.schedular.user.api.dto.UserResponse;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
import com.minidoodle.schedular.user.usecase.RegisterUserUseCase;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final RegisterUserUseCase registerUser;
    private final GetUserUseCase getUser;

    public UserController(RegisterUserUseCase registerUser, GetUserUseCase getUser) {
        this.registerUser = registerUser;
        this.getUser = getUser;
    }

    @PostMapping
    @Operation(summary = "Register a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "422", description = "Invalid input or duplicate email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = UserResponse.from(registerUser.register(request.name(), request.email()));
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponse get(@PathVariable UUID id) {
        return UserResponse.from(getUser.get(new UserId(id)));
    }
}
