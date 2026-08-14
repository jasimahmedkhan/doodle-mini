package com.minidoodle.schedular.slot.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class SlotNotFoundException extends DomainException {

    public SlotNotFoundException(String message) {
        super(message);
    }
}
