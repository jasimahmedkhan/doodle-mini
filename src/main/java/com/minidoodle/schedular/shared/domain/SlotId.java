package com.minidoodle.schedular.shared.domain;

import java.util.Objects;
import java.util.UUID;

public final class SlotId {

    private final UUID value;

    public SlotId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static SlotId random() {
        return new SlotId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlotId slotId)) return false;
        return value.equals(slotId.value);
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
