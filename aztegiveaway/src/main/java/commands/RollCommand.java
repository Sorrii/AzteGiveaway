/**
 * Class that handles the roll command.
 * This command is used to instantly roll a giveaway and select winners
 * Only users with the ADMINISTRATOR permission can use this command
 * You cannot roll a giveaway that has already ended -> use the reroll command instead
 * USAGE: /giveaway roll --giveaway_title "title"
 */

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

import utils.GiveawayUtil;

import jakarta.transaction.Transactional;
import java.util.Optional;
@Component
public class RollCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollCommand.class);

    private final GiveawayService giveawayService;
    private final WinnerService winnerService;

    @Autowired
    public RollCommand(GiveawayService giveawayService, WinnerService winnerService) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
    }

    @Transactional
    public void handleRollCommand(SlashCommandInteractionEvent event) {
        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            event.reply("Missing required option: giveaway_title.").setEphemeral(true).queue();
            return;
        }

        // Retrieve the giveaway by title and initialize entries
        GiveawayEntity giveaway = giveawayService.getGiveawayByTitle(title);
        if (giveaway == null) {
            event.reply("No giveaway found with the title: " + title).setEphemeral(true).queue();
            return;
        }

        // Cancel the scheduled end of the giveaway if it hasn't ended yet
        boolean isEnded = GiveawayUtil.cancelScheduledGiveawayEnd(giveaway);
        if (!isEnded) {
            event.reply("The giveaway " + title + " has already ended.").queue();
            return;
        }

        giveaway = giveawayService.getGiveawayByTitle(title); // Retrieve the giveaway again

        // Roll the giveaway immediately
        GiveawayUtil.endGiveaway(giveaway, event.getJDA(), giveaway.getMessageId(), giveawayService, winnerService);

        event.reply("The giveaway " + title + " has been rolled successfully.").queue();
        LOGGER.info("Giveaway {} has been rolled immediately.", title);
    }
}
