package com.minidoodle.schedular.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeetingPersistenceRepository extends JpaRepository<MeetingEntity, UUID> {

    Optional<MeetingEntity> findBySlotId(UUID slotId);
}
