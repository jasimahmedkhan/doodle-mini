package com.minidoodle.schedular.user.domain;

import com.minidoodle.schedular.shared.domain.UserId;

import java.util.Optional;

/** Persistence port owned by the user domain, not a cross-module API. */
public interface UserRepository {

    boolean existsById(UserId id);

    boolean existsByEmail(String email);

    Optional<User> findById(UserId id);

    User save(User user);
}
