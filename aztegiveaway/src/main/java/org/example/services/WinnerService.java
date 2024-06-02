package org.example.services;

import org.example.entities.WinnerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.repositories.WinnerRepository;

import java.util.List;

@Service
public class WinnerService {
    private final WinnerRepository winnerRepository;
    @Autowired

    public WinnerService(WinnerRepository winnerRepository) {
        this.winnerRepository = winnerRepository;
    }

    public WinnerEntity addWinner(WinnerEntity winner) {
        return winnerRepository.save(winner);
    }

    public List<WinnerEntity> addWinners(List<WinnerEntity> winners) {
        return winnerRepository.saveAll(winners);
    }

    public List<WinnerEntity> getAllWinners() {
        return winnerRepository.findAll();
    }
}
