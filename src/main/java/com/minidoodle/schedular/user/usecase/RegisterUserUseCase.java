package com.minidoodle.schedular.user.usecase;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;
import com.minidoodle.schedular.user.domain.exception.DuplicateEmailException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registers a user while translating concurrent unique-email failures into a domain error. */
@Service
@Transactional
public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Validates and persists a new user with a unique email address. */
    public User register(String name, String email) {
        User user = new User(UserId.random(), name, email);
        if (userRepository.existsByEmail(user.email())) {
            throw new DuplicateEmailException("A user with email already exists: " + user.email());
        }
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException conflict) {
            throw new DuplicateEmailException("A user with email already exists: " + user.email());
        }
    }
}
