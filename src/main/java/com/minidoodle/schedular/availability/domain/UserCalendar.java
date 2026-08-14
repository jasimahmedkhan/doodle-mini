package com.minidoodle.schedular.availability.domain;

import com.minidoodle.schedular.shared.domain.TimeRange;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Derived availability view over slots. It is calculated on demand and is never persisted. */
public final class UserCalendar {

    private static final Comparator<SlotView> BY_START = Comparator
            .comparing((SlotView slot) -> slot.timeRange().start())
            .thenComparing(slot -> slot.timeRange().end());

    private final List<SlotView> slots;

    public UserCalendar(List<SlotView> slots) {
        Objects.requireNonNull(slots, "slots must not be null");
        for (SlotView slot : slots) {
            Objects.requireNonNull(slot, "slots must not contain null");
        }
        this.slots = slots.stream()
                .sorted(BY_START)
                .toList();
    }

    public Availability availability(TimeRange requestedWindow) {
        Objects.requireNonNull(requestedWindow, "requestedWindow must not be null");

        // Sorting in the constructor makes clipping and merging a single forward pass.
        List<AvailabilityRange> result = new ArrayList<>();
        for (SlotView slot : slots) {
            if (!slot.timeRange().overlaps(requestedWindow)) {
                continue;
            }

            AvailabilityRange clipped = new AvailabilityRange(
                    clip(slot.timeRange(), requestedWindow),
                    slot.status()
            );
            appendOrMerge(result, clipped);
        }
        return new Availability(result);
    }

    public List<SlotView> slots() {
        return slots;
    }

    private static TimeRange clip(TimeRange range, TimeRange window) {
        Instant start = range.start().isBefore(window.start()) ? window.start() : range.start();
        Instant end = range.end().isAfter(window.end()) ? window.end() : range.end();
        return new TimeRange(start, end);
    }

    private static void appendOrMerge(List<AvailabilityRange> result, AvailabilityRange current) {
        if (result.isEmpty()) {
            result.add(current);
            return;
        }

        int lastIndex = result.size() - 1;
        AvailabilityRange previous = result.get(lastIndex);
        boolean sameStatus = previous.status() == current.status();
        boolean adjacent = previous.timeRange().end().equals(current.timeRange().start());

        if (sameStatus && adjacent) {
            // Gaps and status changes remain visible; only true adjacency is merged.
            result.set(lastIndex, new AvailabilityRange(
                    new TimeRange(previous.timeRange().start(), current.timeRange().end()),
                    current.status()
            ));
        } else {
            result.add(current);
        }
    }
}
