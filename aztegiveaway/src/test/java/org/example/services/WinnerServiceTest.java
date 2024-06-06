package org.example.services;

import org.example.entities.WinnerEntity;
import org.example.repositories.WinnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WinnerServiceTest {

    @Mock
    private WinnerRepository winnerRepository;

    @InjectMocks
    private WinnerService winnerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddWinner() {
        WinnerEntity winner = new WinnerEntity();
        winnerService.addWinner(winner);
        verify(winnerRepository, times(1)).save(winner);
    }

    @Test
    void testGetWinnersByGuildId() {
        Long guildId = 1L;
        List<WinnerEntity> winners = List.of(new WinnerEntity(), new WinnerEntity());
        when(winnerRepository.findByGuildId(guildId)).thenReturn(winners);

        List<WinnerEntity> result = winnerService.getWinnersByGuildId(guildId);
        assertEquals(2, result.size());
        verify(winnerRepository, times(1)).findByGuildId(guildId);
    }

    @Test
    void testGetWinnersByGiveawayMessageIdAndGuildId() {
        Long giveawayMessageId = 1L;
        Long guildId = 1L;
        List<WinnerEntity> winners = List.of(new WinnerEntity(), new WinnerEntity());
        when(winnerRepository.findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId)).thenReturn(winners);

        List<WinnerEntity> result = winnerService.getWinnersByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
        assertEquals(2, result.size());
        verify(winnerRepository, times(1)).findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);
    }

    @Test
    void testDeleteWinnersByGiveawayMessageIdAndGuildId() {
        Long giveawayMessageId = 1L;
        Long guildId = 1L;

        WinnerEntity winner1 = new WinnerEntity("giveawayTitle1", giveawayMessageId, 123L, guildId);
        WinnerEntity winner2 = new WinnerEntity("giveawayTitle1", giveawayMessageId, 456L, guildId);

        when(winnerRepository.findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId))
                .thenReturn(List.of(winner1, winner2));
        doNothing().when(winnerRepository).deleteByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);

        winnerService.deleteWinnersByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);

        verify(winnerRepository, times(1)).deleteByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);

        when(winnerRepository.findByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId)).thenReturn(List.of());

        List<WinnerEntity> winnersAfterDeletion = winnerService.getWinnersByGiveawayMessageIdAndGuildId(giveawayMessageId, guildId);

        assertTrue(winnersAfterDeletion.isEmpty());
    }
}

