package com.minidoodle.schedular.user.domain;

import com.minidoodle.schedular.shared.domain.UserId;

import java.util.Optional;

/** Persistence port owned by the user domain, infrastructure layer implements it. */
public interface UserRepository {

    boolean existsById(UserId id);

    boolean existsByEmail(String email);

    Optional<User> findById(UserId id);

    User save(User user);
}
