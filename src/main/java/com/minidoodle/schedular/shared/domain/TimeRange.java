package com.minidoodle.schedular.shared.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * An immutable half-open time range: start is inclusive and end is exclusive.
 * This convention allows adjacent ranges without treating them as overlapping.
 */
public final class TimeRange {

    private final Instant start;
    private final Instant end;

    public TimeRange(Instant start, Instant end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
        this.start = start;
        this.end = end;
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /**
     * Two ranges overlap if they share at least one instant.
     * Adjacent ranges do not overlap.
     */
    public boolean overlaps(TimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeRange that)) return false;
        return start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "TimeRange[" + start + "," + end + "]";
    }
}
