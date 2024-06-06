package org.example.services;

import org.example.entities.GiveawayEntity;
import org.example.repositories.GiveawayRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GiveawayServiceTest {

    @Mock
    private GiveawayRepository giveawayRepository;

    @InjectMocks
    private GiveawayService giveawayService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGiveaway() {
        GiveawayEntity giveaway = new GiveawayEntity();
        giveawayService.createGiveaway(giveaway);
        verify(giveawayRepository, times(1)).save(giveaway);
    }

    @Test
    void testGetGiveawayByMessageId() {
        GiveawayEntity giveaway = new GiveawayEntity();
        when(giveawayRepository.findByMessageId(1L)).thenReturn(giveaway);

        GiveawayEntity result = giveawayService.getGiveawayByMessageId(1L);
        assertNotNull(result);
        verify(giveawayRepository, times(1)).findByMessageId(1L);
    }

    @Test
    void testGetAllGiveaways() {
        List<GiveawayEntity> giveaways = List.of(new GiveawayEntity(), new GiveawayEntity());
        when(giveawayRepository.findAll()).thenReturn(giveaways);

        List<GiveawayEntity> result = giveawayService.getAllGiveaways();
        assertEquals(2, result.size());
        verify(giveawayRepository, times(1)).findAll();
    }

    @Test
    void testDeleteGiveaway() {
        when(giveawayRepository.existsById(1L)).thenReturn(true);
        doNothing().when(giveawayRepository).deleteById(1L);

        giveawayService.deleteGiveaway(1L);
        verify(giveawayRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateGiveaway() {
        GiveawayEntity giveaway = new GiveawayEntity();
        giveaway.setId(1L);
        when(giveawayRepository.existsById(1L)).thenReturn(true);

        giveawayService.updateGiveaway(giveaway);
        verify(giveawayRepository, times(1)).save(giveaway);
    }

    @Test
    void testGetGiveawayEntries() {
        GiveawayEntity giveaway = new GiveawayEntity();
        giveaway.setId(1L);
        giveaway.setEntries(List.of(1L, 2L, 3L));
        when(giveawayRepository.findById(1L)).thenReturn(Optional.of(giveaway));

        List<Long> entries = giveawayService.getGiveawayEntries(1L);
        assertEquals(3, entries.size());
        verify(giveawayRepository, times(1)).findById(1L);
    }
}
