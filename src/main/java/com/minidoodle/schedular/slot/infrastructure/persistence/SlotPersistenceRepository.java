package com.minidoodle.schedular.slot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlotPersistenceRepository extends JpaRepository<SlotEntity, UUID> {


}
