package com.minidoodle.schedular.user.usecase;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;
import com.minidoodle.schedular.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Retrieves a user or raises the module's stable not-found exception. */
@Service
@Transactional(readOnly = true)
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns the requested user when it exists. */
    public User get(UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
