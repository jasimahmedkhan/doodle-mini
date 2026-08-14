package com.minidoodle.schedular.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingPersistenceRepository extends JpaRepository<MeetingEntity, UUID> {

    Optional<MeetingEntity> findBySlotId(UUID slotId);

    @Query("""
            select distinct meeting
            from MeetingEntity meeting
            left join fetch meeting.participants
            where meeting.slotId in :slotIds
            """)
    List<MeetingEntity> findAllBySlotIdIn(@Param("slotIds") Collection<UUID> slotIds);
}
