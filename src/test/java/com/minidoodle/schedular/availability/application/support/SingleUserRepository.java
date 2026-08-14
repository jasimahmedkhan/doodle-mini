package com.minidoodle.schedular.availability.application.support;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;

import java.util.Optional;

public final class SingleUserRepository implements UserRepository {

    private final User user;

    public SingleUserRepository(User user) {
        this.user = user;
    }

    @Override
    public boolean existsById(UserId id) {
        return user != null && user.id().equals(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return user != null && user.email().equals(email);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return existsById(id) ? Optional.of(user) : Optional.empty();
    }

    @Override
    public User save(User user) {
        throw new UnsupportedOperationException("This test repository is read-only");
    }
}
