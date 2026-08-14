package com.minidoodle.schedular.slot.domain.exception;

import com.minidoodle.schedular.shared.domain.exception.DomainException;

public class SlotOverlapException extends DomainException {

    public SlotOverlapException(String message) {
        super(message);
    }
}
