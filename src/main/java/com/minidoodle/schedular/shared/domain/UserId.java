package com.minidoodle.schedular.shared.domain;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;

    public UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return value.equals(userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
