package com.minidoodle.schedular.user.domain;

import com.minidoodle.schedular.shared.domain.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createsUser() {
        UserId id = UserId.random();
        User user = new User(id, "Alice", "alice@example.com");

        assertEquals(id, user.id());
        assertEquals("Alice", user.name());
        assertEquals("alice@example.com", user.email());
    }

    @Test
    void rejectsMissingIdentityOrName() {
        assertThrows(NullPointerException.class,
                () -> new User(null, "Alice", "alice@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new User(UserId.random(), " ", "alice@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-an-email", "alice@", "alice@example..com", "alice@-example.com"})
    void rejectsInvalidEmail(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new User(UserId.random(), "Alice", email));
    }

    @Test
    void equalityUsesIdentityOnly() {
        UserId id = UserId.random();

        assertEquals(
                new User(id, "Alice", "alice@example.com"),
                new User(id, "Changed", "changed@example.com")
        );
        assertNotEquals(
                new User(UserId.random(), "Alice", "alice@example.com"),
                new User(UserId.random(), "Alice", "alice@example.com")
        );
    }
}
