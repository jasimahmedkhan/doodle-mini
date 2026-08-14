package com.minidoodle.schedular.meeting.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> new Participant(name, "alice@example.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingEmail(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new Participant("Alice", email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-an-email",
            "@example.com",
            "alice@",
            "alice@.com",
            "alice@example..com",
            "alice@-example.com",
            "alice.example.com"
    })
    void rejectsInvalidEmailFormat(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new Participant("Alice", email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "alice@example.com",
            "alice.smith@example.com",
            "alice+tag@example.com",
            "alice@sub.example.co.uk",
            "alice_smith@example.com",
            "123@example.com"
    })
    void acceptsValidEmailFormat(String email) {
        assertDoesNotThrow(() -> new Participant("Alice", email));
    }

    @Test
    void equalsAndHashCodeByValue() {
        Participant a = new Participant("Alice", "alice@example.com");
        Participant b = new Participant("Alice", "alice@example.com");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentParticipantsAreNotEqual() {
        Participant a = new Participant("Alice", "alice@example.com");
        Participant b = new Participant("Bob", "alice@example.com");

        assertNotEquals(a, b);
    }
}
