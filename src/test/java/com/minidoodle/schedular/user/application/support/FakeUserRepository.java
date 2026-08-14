package com.minidoodle.schedular.user.application.support;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class FakeUserRepository implements UserRepository {

    private final Map<UserId, User> users = new HashMap<>();

    @Override
    public boolean existsById(UserId id) {
        return users.containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.email().equals(email));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User save(User user) {
        users.put(user.id(), user);
        return user;
    }
}
