package com.minidoodle.schedular.user.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
