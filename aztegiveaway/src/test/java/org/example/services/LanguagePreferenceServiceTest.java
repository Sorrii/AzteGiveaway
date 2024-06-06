package org.example.services;

import org.example.entities.LanguagePreferenceEntity;
import org.example.repositories.LanguagePreferenceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LanguagePreferenceServiceTest {

    @Mock
    private LanguagePreferenceRepository languagePreferenceRepository;

    @InjectMocks
    private LanguagePreferenceService languagePreferenceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetLanguagePreference_NewPreference() {
        Long guildId = 1L;
        String language = "fr";

        when(languagePreferenceRepository.findByGuildId(guildId)).thenReturn(null);

        languagePreferenceService.setLanguagePreference(guildId, language);

        verify(languagePreferenceRepository, times(1)).save(any(LanguagePreferenceEntity.class));
    }

    @Test
    void testSetLanguagePreference_UpdatePreference() {
        Long guildId = 1L;
        String language = "fr";
        LanguagePreferenceEntity existingPreference = new LanguagePreferenceEntity(guildId, "en");

        when(languagePreferenceRepository.findByGuildId(guildId)).thenReturn(existingPreference);

        languagePreferenceService.setLanguagePreference(guildId, language);

        assertEquals(language, existingPreference.getLanguage());
        verify(languagePreferenceRepository, times(1)).save(existingPreference);
    }

    @Test
    void testGetLanguagePreference_ExistingPreference() {
        Long guildId = 1L;
        String language = "fr";
        LanguagePreferenceEntity existingPreference = new LanguagePreferenceEntity(guildId, language);

        when(languagePreferenceRepository.findByGuildId(guildId)).thenReturn(existingPreference);

        String result = languagePreferenceService.getLanguagePreference(guildId);

        assertEquals(language, result);
        verify(languagePreferenceRepository, times(1)).findByGuildId(guildId);
    }

    @Test
    void testGetLanguagePreference_DefaultPreference() {
        Long guildId = 1L;

        when(languagePreferenceRepository.findByGuildId(guildId)).thenReturn(null);

        String result = languagePreferenceService.getLanguagePreference(guildId);

        assertEquals("en", result);
        verify(languagePreferenceRepository, times(1)).findByGuildId(guildId);
    }
}
