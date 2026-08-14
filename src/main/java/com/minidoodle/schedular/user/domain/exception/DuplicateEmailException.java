package com.minidoodle.schedular.user.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class DuplicateEmailException extends DomainException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
