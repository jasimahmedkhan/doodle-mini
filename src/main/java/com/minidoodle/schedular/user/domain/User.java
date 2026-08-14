package com.minidoodle.schedular.user.domain;

import com.minidoodle.schedular.shared.domain.UserId;

import java.util.Objects;

public final class User {

    private final UserId id;
    private final String name;
    // email should be validated
    private final String email;

    public User(UserId id, String name, String email) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireText(name, "name");
        this.email = email;
    }

    public UserId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User[" + id + ", " + name + ", " + email + "]";
    }
}
