package org.example.repositories;

import org.example.entities.WinnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface WinnerRepository extends JpaRepository<WinnerEntity, Long> {
}
