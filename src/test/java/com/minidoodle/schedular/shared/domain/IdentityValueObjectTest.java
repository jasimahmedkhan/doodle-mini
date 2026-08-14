package com.minidoodle.schedular.shared.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityValueObjectTest {

    @Test
    void identitiesRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new UserId(null));
        assertThrows(NullPointerException.class, () -> new SlotId(null));
        assertThrows(NullPointerException.class, () -> new MeetingId(null));
    }
}
