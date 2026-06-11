package com.scriptorium.scriptorium.repository;

import com.scriptorium.scriptorium.domain.Curator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuratorRepository extends JpaRepository<Curator,Long> {

    Optional<Curator> findByEmail(String email);

}
