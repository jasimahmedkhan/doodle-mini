package com.minidoodle.schedular.slot.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class SlotNotBookableException extends DomainException {

    public SlotNotBookableException(String message) {
        super(message);
    }
}
