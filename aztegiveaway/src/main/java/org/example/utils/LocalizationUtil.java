/**
 * Localization Utility class to get localized messages
 * It uses the LocaleConfig to get the locale for a guild and ResourceBundle to get the messages for that locale.
 */

package org.example.utils;

import org.example.config.LocaleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class LocalizationUtil {

    private final LocaleConfig localeConfig;

    @Autowired
    public LocalizationUtil(LocaleConfig localeConfig) {
        this.localeConfig = localeConfig;
    }

    public String getLocalizedMessage(Long guildId, String key) {
        Locale locale = localeConfig.getLocaleForGuild(guildId);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        return messages.getString(key);
    }
}
