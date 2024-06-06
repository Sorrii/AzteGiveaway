package org.example.services;

import org.example.entities.WinnerEntity;
import org.example.repositories.WinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class WinnerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinnerService.class);
    private final WinnerRepository winnerRepository;

    @Autowired
    public WinnerService(WinnerRepository winnerRepository) {
        this.winnerRepository = winnerRepository;
    }

    @Transactional
    public void addWinner(WinnerEntity winner) {
        Optional<WinnerEntity> existingWinner = winnerRepository.findByGiveawayMessageIdAndUserIdAndGuildId(
                winner.getGiveawayMessageId(), winner.getUserId(), winner.getGuildId());
        if (existingWinner.isPresent()) {
            LOGGER.warn("Winner already exists: {}", existingWinner.get());
        } else {
            winnerRepository.save(winner);
            LOGGER.info("Added new winner: {}", winner);
        }
    }

    @Transactional(readOnly = true)
    public List<WinnerEntity> getWinnersByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId) {
        return winnerRepository.findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
    }

    @Transactional(readOnly = true)
    public List<WinnerEntity> getWinnersByGuildId(Long guildId) {
        return winnerRepository.findByGuildId(guildId);
    }

    @Transactional
    public void deleteWinnersByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId) {
        winnerRepository.deleteByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
    }
}
