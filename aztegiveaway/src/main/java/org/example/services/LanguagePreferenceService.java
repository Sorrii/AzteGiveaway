package org.example.services;

import org.example.entities.LanguagePreferenceEntity;
import org.example.repositories.LanguagePreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LanguagePreferenceService {

    private final LanguagePreferenceRepository languagePreferenceRepository;

    @Autowired
    public LanguagePreferenceService(LanguagePreferenceRepository languagePreferenceRepository) {
        this.languagePreferenceRepository = languagePreferenceRepository;
    }

    public void setLanguagePreference(Long guildId, String language) {
        LanguagePreferenceEntity preference = languagePreferenceRepository.findByGuildId(guildId);
        if (preference == null) {
            preference = new LanguagePreferenceEntity(guildId, language);
        } else {
            preference.setLanguage(language);
        }
        languagePreferenceRepository.save(preference);
    }

    public String getLanguagePreference(Long guildId) {
        LanguagePreferenceEntity preference = languagePreferenceRepository.findByGuildId(guildId);
        return (preference != null) ? preference.getLanguage() : "en";
    }
}