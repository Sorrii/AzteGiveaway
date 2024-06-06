package org.example.services;

import org.example.entities.WinnerEntity;
import org.example.repositories.WinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WinnerService {

    private final WinnerRepository winnerRepository;

    @Autowired
    public WinnerService(WinnerRepository winnerRepository) {
        this.winnerRepository = winnerRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addWinner(WinnerEntity winner) {
        winnerRepository.save(winner);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public List<WinnerEntity> getWinnersByGuildId(Long guildId) {
        return winnerRepository.findByGuildId(guildId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public List<WinnerEntity> getWinnersByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId) {
        return winnerRepository.findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public void deleteWinnersByGiveawayMessageIdAndGuildId(Long giveawayMessageId, Long guildId) {
        winnerRepository.deleteByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
    }
}
