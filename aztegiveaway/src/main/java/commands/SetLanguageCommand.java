package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.example.services.LanguagePreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.utils.LocalizationUtil;

import java.util.Objects;

@Component
public class SetLanguageCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetLanguageCommand.class);

    private final LanguagePreferenceService languagePreferenceService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public SetLanguageCommand(LanguagePreferenceService languagePreferenceService, LocalizationUtil localizationUtil) {
        this.languagePreferenceService = languagePreferenceService;
        this.localizationUtil = localizationUtil;
    }

    public void handleSetLanguageCommand(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            LOGGER.warn("User {} does not have permission to use the set language command", event.getUser());
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_permission")).setEphemeral(true).queue();
            return;
        }

        String language = Objects.requireNonNull(event.getOption("language")).getAsString();
        if (!language.equals("en") && !language.equals("ro")) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "invalid_language_option")).setEphemeral(true).queue();
            return;
        }

        languagePreferenceService.setLanguagePreference(guildId, language);
        event.reply(localizationUtil.getLocalizedMessage(guildId, "language_set_success").replace("{0}", language)).setEphemeral(true).queue();
        LOGGER.info("Language preference set to {} for guild {}", language, guildId);
    }
}
