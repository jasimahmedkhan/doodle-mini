package com.minidoodle.schedular.availability.domain;

/**
 * Read-side slot status used by the availability domain.
 * It deliberately does not depend on the slot module's domain model.
 */
public enum SlotStatus {
    FREE,
    BUSY,
    BOOKED
}
