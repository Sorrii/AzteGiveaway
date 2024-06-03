package org.example.repositories;

import org.example.entities.WinnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface WinnerRepository extends JpaRepository<WinnerEntity, Long> {
    List<WinnerEntity> findByGiveawayTitle(String giveawayTitle);

    void deleteByGiveawayTitle(String giveawayTitle);
}
