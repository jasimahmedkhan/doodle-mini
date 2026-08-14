package com.minidoodle.schedular.slot.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class SlotNotModifiableException extends DomainException {

    public SlotNotModifiableException(String message) {
        super(message);
    }
}
