/**
 * Class that handles the winners command
 * This command is used to retrieve the winners of a specific giveaway
 * Only users with the ADMINISTRATOR permission can use this command
 * USAGE: /giveaway winners --giveaway_title "title"
 */

package commands;

import org.example.entities.WinnerEntity;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WinnersCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinnersCommand.class);

    private final WinnerService winnerService;

    @Autowired
    public WinnersCommand(WinnerService winnerService) {
        this.winnerService = winnerService;
    }

    public void handleWinnersCommand(SlashCommandInteractionEvent event) {
        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use that command!").setEphemeral(true).queue();
            return;
        }

        // Get the giveaway title option and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            event.reply("Missing required option: giveaway_title.").setEphemeral(true).queue();
            return;
        }

        // Retrieving the winners by giveaway title
        List<WinnerEntity> winners = winnerService.getWinnersByGiveawayTitle(title);
        if (winners.isEmpty()) {
            event.reply("No winners found for the giveaway with the title: " + title).setEphemeral(true).queue();
            return;
        }

        StringBuilder winnerMessage = new StringBuilder("Winners of the giveaway " + title + ":\n");
        for (WinnerEntity winner : winners) {
            winnerMessage.append("<@").append(winner.getUserId()).append(">\n");
        }

        event.reply(winnerMessage.toString()).queue();
        LOGGER.info("Winners for giveaway {}: {}", title, winners);
    }
}
