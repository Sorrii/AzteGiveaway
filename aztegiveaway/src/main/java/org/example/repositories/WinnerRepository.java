package org.example.repositories;

import org.example.entities.WinnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WinnerRepository extends JpaRepository<WinnerEntity, Long> {
    List<WinnerEntity> findByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId);
    List<WinnerEntity> findByGuildId(Long guildId);
    void deleteByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId);
    Optional<WinnerEntity> findByGiveawayMessageIdAndUserIdAndGuildId(Long giveawayMessageId, Long userId, Long guildId); // New method
}
