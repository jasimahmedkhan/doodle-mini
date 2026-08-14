package com.minidoodle.schedular.slot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SlotPersistenceRepository extends JpaRepository<SlotEntity, UUID> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM time_slot s
                WHERE s.owner_id = :ownerId
                  AND s.start_ts < :rangeEnd
                  AND :rangeStart < s.end_ts
            )
            """, nativeQuery = true)
    boolean existsOverlapping(
            @Param("ownerId") UUID ownerId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd
    );

    @Query("""
            SELECT s
            FROM SlotEntity s
            WHERE s.ownerId = :ownerId
              AND s.start < :rangeEnd
              AND :rangeStart < s.end
            ORDER BY s.start, s.end
            """)
    List<SlotEntity> findOverlapping(
            @Param("ownerId") UUID ownerId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd
    );
}
