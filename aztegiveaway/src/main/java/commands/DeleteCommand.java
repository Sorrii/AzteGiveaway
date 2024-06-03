/**
 * Class that handles the delete command.
 * This command is used to delete a giveaway from the database
 * Only users with the ADMINISTRATOR permission can use this command
 * USAGE: /giveaway delete --giveaway_title "title"
 */

package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

import utils.GiveawayUtil;

@Component
public class DeleteCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCommand.class);

    private final GiveawayService giveawayService;

    @Autowired
    public DeleteCommand(GiveawayService giveawayService) {
        this.giveawayService = giveawayService;
    }

    public void handleDeleteCommand(SlashCommandInteractionEvent event) {
        // IMPORTANT Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
            return;
        }

        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            event.reply("Missing required option: giveaway_title.").setEphemeral(true).queue();
            return;
        }

        // Delete the giveaway
        GiveawayEntity giveaway = giveawayService.getGiveawayByTitle(title);
        if (giveaway == null) {
            event.reply("No giveaway found with the title: " + title).setEphemeral(true).queue();
            return;
        }
        // Cancel the scheduled end for the giveaway
        GiveawayUtil.cancelScheduledGiveawayEnd(giveaway);

        giveawayService.deleteGiveaway(giveaway.getId());
        event.reply("Giveaway with the title '" + title + "' has been deleted from the database.").setEphemeral(true).queue();
        LOGGER.info("Giveaway with title {} has been deleted from the database.", title);
    }
}
