package com.minidoodle.schedular.meeting.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class MeetingNotFoundException extends DomainException {

    public MeetingNotFoundException(String message) {
        super(message);
    }
}
