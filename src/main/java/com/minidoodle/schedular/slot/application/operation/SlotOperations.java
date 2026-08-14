package com.minidoodle.schedular.slot.application.operation;

import com.minidoodle.schedular.shared.domain.SlotId;


public interface SlotOperations {

    void reserve(SlotId slotId);

    void release(SlotId slotId);
}
