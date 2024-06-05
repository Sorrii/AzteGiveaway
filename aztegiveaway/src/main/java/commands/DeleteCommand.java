/**
 * Class that handles the delete command.
 * This command is used to delete a giveaway from the database
 * Only users with the ADMINISTRATOR permission can use this command
 * USAGE: /giveaway delete --giveaway_title "title"
 */

package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.example.utils.GiveawayUtil;
import org.example.utils.LocalizationUtil;

import java.text.MessageFormat;
import java.util.Optional;

@Component
public class DeleteCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCommand.class);

    private final GiveawayService giveawayService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public DeleteCommand(GiveawayService giveawayService, LocalizationUtil localizationUtil) {
        this.giveawayService = giveawayService;
        this.localizationUtil = localizationUtil;
    }

    public void handleDeleteCommand(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            LOGGER.warn("User {} does not have permission to use the giveaway command", event.getUser());
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_permission")).setEphemeral(true).queue();
            return;
        }

        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            LOGGER.warn("User {} did not provide a title for the giveaway. Deletion failed", event.getUser());
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_title")).setEphemeral(true).queue();
            return;
        }

        // Delete the giveaway
        GiveawayEntity giveaway = giveawayService.getGiveawayByTitleAndGuildId(title, guildId);
        if (giveaway == null) {
            LOGGER.warn("Giveaway with title {} not found in the database", title);
            event.reply(MessageFormat.format(localizationUtil.getLocalizedMessage(guildId, "no_giveaway_found"), title)).setEphemeral(true).queue();
            return;
        }
        // Cancel the scheduled end for the giveaway
        GiveawayUtil.cancelScheduledGiveawayEnd(giveaway);

        giveawayService.deleteGiveaway(giveaway.getId());
        event.reply(MessageFormat.format(localizationUtil.getLocalizedMessage(guildId, "giveaway_deleted"), title)).setEphemeral(true).queue();
        LOGGER.info("Giveaway with title {} has been deleted from the database.", title);
    }
}