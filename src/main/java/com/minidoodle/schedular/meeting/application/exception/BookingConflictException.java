package com.minidoodle.schedular.meeting.application.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class BookingConflictException extends DomainException {

    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
