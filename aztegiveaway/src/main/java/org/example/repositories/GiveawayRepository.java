package org.example.repositories;

import org.example.entities.GiveawayEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiveawayRepository extends JpaRepository<GiveawayEntity, Long> {
    @EntityGraph(attributePaths = {"entries"})
    GiveawayEntity findByMessageId(Long messageId);

    @EntityGraph(attributePaths = {"entries"})
    GiveawayEntity findByTitle(String title);
}
