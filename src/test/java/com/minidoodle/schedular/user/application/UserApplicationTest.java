package com.minidoodle.schedular.user.application;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.application.support.FakeUserRepository;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.exception.DuplicateEmailException;
import com.minidoodle.schedular.user.domain.exception.UserNotFoundException;
import com.minidoodle.schedular.user.usecase.GetUserUseCase;
import com.minidoodle.schedular.user.usecase.RegisterUserUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserApplicationTest {

    @Test
    void registersAndGetsUser() {
        FakeUserRepository users = new FakeUserRepository();
        User registered = new RegisterUserUseCase(users).register("Alice", "alice@example.com");

        assertEquals(registered, new GetUserUseCase(users).get(registered.id()));
    }

    @Test
    void rejectsDuplicateEmail() {
        FakeUserRepository users = new FakeUserRepository();
        RegisterUserUseCase registration = new RegisterUserUseCase(users);
        registration.register("Alice", "alice@example.com");

        assertThrows(
                DuplicateEmailException.class,
                () -> registration.register("Another Alice", "alice@example.com")
        );
    }

    @Test
    void getThrowsWhenUserDoesNotExist() {
        assertThrows(
                UserNotFoundException.class,
                () -> new GetUserUseCase(new FakeUserRepository()).get(UserId.random())
        );
    }

}
