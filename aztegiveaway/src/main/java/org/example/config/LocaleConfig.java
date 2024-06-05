package org.example.config;

import org.example.services.LanguagePreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleConfig {

    private final LanguagePreferenceService languagePreferenceService;

    @Autowired
    public LocaleConfig(LanguagePreferenceService languagePreferenceService) {
        this.languagePreferenceService = languagePreferenceService;
    }

    public Locale getLocaleForGuild(Long guildId) {
        String language = languagePreferenceService.getLanguagePreference(guildId);
        if ("ro".equals(language)) {
            return new Locale.Builder().setLanguage("ro").setRegion("RO").build();
        } else {
            return new Locale.Builder().setLanguage("en").setRegion("US").build();
        }
    }
}