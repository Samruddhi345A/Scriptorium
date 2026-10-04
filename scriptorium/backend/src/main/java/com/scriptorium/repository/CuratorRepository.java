package com.scriptorium.repository;

import com.scriptorium.domain.CuratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CuratorRepository extends JpaRepository<CuratorEntity, UUID> {

    Optional<CuratorEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
