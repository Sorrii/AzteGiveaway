package org.example.repositories;

import org.example.entities.GiveawayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiveawayRepository extends JpaRepository<GiveawayEntity, Long> {
    GiveawayEntity findByMessageId(Long messageId);
    GiveawayEntity findByTitle(String title);
}
