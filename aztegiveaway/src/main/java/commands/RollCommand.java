package commands;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.example.utils.GiveawayUtil;
import org.example.utils.LocalizationUtil;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Component
public class RollCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollCommand.class);

    private final GiveawayService giveawayService;
    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public RollCommand(GiveawayService giveawayService, WinnerService winnerService, LocalizationUtil localizationUtil) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
        this.localizationUtil = localizationUtil;
    }

    @Transactional
    public void handleRollCommand(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        // Get the guild ID
        Long guildId = event.getGuild().getIdLong();

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_permission")).setEphemeral(true).queue();
            LOGGER.warn("User {} does not have permission to use the roll command", event.getUser());
            return;
        }

        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_title")).setEphemeral(true).queue();
            LOGGER.warn("User {} did not provide a title for the giveaway. Rolling failed", event.getUser());
            return;
        }

        // Retrieve the giveaway by title and initialize entries
        GiveawayEntity giveaway = giveawayService.getGiveawayByTitleAndGuildId(title, guildId);
        if (giveaway == null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_giveaway_found") + ": " + title).setEphemeral(true).queue();
            LOGGER.warn("Giveaway with title {} not found in the database", title);
            return;
        }

        // Cancel the scheduled end of the giveaway if it hasn't ended yet
        boolean isEnded = GiveawayUtil.cancelScheduledGiveawayEnd(giveaway);
        if (!isEnded) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "giveaway_already_ended").replace("{0}", title)).queue();
            return;
        }

        giveaway = giveawayService.getGiveawayByTitleAndGuildId(title, guildId); // Retrieve the giveaway again
        GiveawayUtil giveawayUtil = new GiveawayUtil(localizationUtil);
        // Roll the giveaway immediately
        giveawayUtil.endGiveaway(giveaway, event.getJDA(), giveaway.getMessageId(), giveawayService, winnerService);

        event.reply(localizationUtil.getLocalizedMessage(guildId, "giveaway_rolled_success").replace("{0}", title)).queue();
        LOGGER.info("Giveaway {} has been rolled immediately.", title);
    }
}
