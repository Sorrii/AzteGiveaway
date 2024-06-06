/**
 * When you annotate a repository method with @EntityGraph, Spring Data JPA will generate a query that fetches the main entity
 * and its related entities in a single query using a join.
 * This can help reduce the number of queries executed by your application.
 */

package org.example.repositories;

import org.example.entities.GiveawayEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiveawayRepository extends JpaRepository<GiveawayEntity, Long> {
    @EntityGraph(attributePaths = {"entries"})
    Optional<GiveawayEntity> findByMessageId(Long messageId);

    @EntityGraph(attributePaths = {"entries"})
    Optional<GiveawayEntity> findByTitleAndGuildId(String title, Long guildId);
}
