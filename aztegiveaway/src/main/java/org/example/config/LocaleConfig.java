/**
 * This class is responsible for providing the locale based on the guild's language preference.
 * It uses the LanguagePreferenceService to get the language preference for a guild.
 * If the language preference is "ro", it returns a Romanian locale, otherwise it returns an English locale.
 */

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

    // returns the locale based on the guild's language preference
    public Locale getLocaleForGuild(Long guildId) {
        String language = languagePreferenceService.getLanguagePreference(guildId);
        if ("ro".equals(language)) {
            return new Locale.Builder().setLanguage("ro").setRegion("RO").build();
        } else {
            return new Locale.Builder().setLanguage("en").setRegion("US").build();
        }
    }
}