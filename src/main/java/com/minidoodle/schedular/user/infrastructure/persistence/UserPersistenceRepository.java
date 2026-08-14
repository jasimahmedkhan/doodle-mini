package com.minidoodle.schedular.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPersistenceRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
}
