package com.minidoodle.schedular.slot.infrastructure.persistence;

import com.minidoodle.schedular.slot.domain.SlotStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_slot")
public class SlotEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "start_ts", nullable = false)
    private Instant start;

    @Column(name = "end_ts", nullable = false)
    private Instant end;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SlotStatus status;

    // Optimistic locking lets concurrent booking contenders fail without serializing normal writes.
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected SlotEntity() {
    }

    public SlotEntity(
            UUID id,
            UUID ownerId,
            Instant start,
            Instant end,
            SlotStatus status,
            long version
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.start = start;
        this.end = end;
        this.status = status;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
