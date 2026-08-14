package com.minidoodle.schedular.shared.domain;

import java.util.Objects;
import java.util.UUID;

public final class MeetingId {

    private final UUID value;

    public MeetingId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static MeetingId random() {
        return new MeetingId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeetingId meetingId)) return false;
        return value.equals(meetingId.value);
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
