/**
 * Class that handles the reroll command
 * This command is used to reroll a giveaway and select new winners
 * If there are no eligible entries left, the previous winners are kept
 * If there are fewer eligible entries than the number of winners, they are selected as the new winners
 * Only users with the ADMINISTRATOR permission can use this command
 * You can specify the number of new winners to select (optional)
 * If the number of new winners is not specified, the original number of winners is used
 * USAGE: /giveaway reroll --giveaway_title "title" [--number_of_new_winners "number_of_new_winners"]
 */

package commands;

import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import utils.FairRandomizer;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RerollCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RerollCommand.class);

    private final GiveawayService giveawayService;
    private final WinnerService winnerService;

    @Autowired
    public RerollCommand(GiveawayService giveawayService, WinnerService winnerService) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
    }

    @Transactional
    public void handleRerollCommand(SlashCommandInteractionEvent event) {
        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use that command!").setEphemeral(true).queue();
            return;
        }

        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Number of winners can be null and if so it's set to the original number of winners
        final Integer newWinners = Optional.ofNullable(event.getOption("number_of_new_winners"))
                .map(OptionMapping::getAsInt)
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
        giveaway.getEntries().size(); // Initialize the entries collection

        // Set the number of winners to the new number of winners if it's not null else set it to the original number of winners
        int winnersCount = newWinners != null ? newWinners : giveaway.getNumberOfWinners();

        // Retrieving previous winners to exclude them from the new draw
        List<WinnerEntity> previousWinners = winnerService.getWinnersByGiveawayTitle(title);
        Set<Long> previousWinnerIds = previousWinners.stream()
                .map(WinnerEntity::getUserId)
                .collect(Collectors.toSet());

        // Filtering out previous winners from the entries
        List<Long> eligibleEntries = giveaway.getEntries().stream()
                .filter(entry -> !previousWinnerIds.contains(entry))
                .collect(Collectors.toList());

        // Check if there are any eligible entries left
        List<Long> winners;
        if (eligibleEntries.isEmpty()) {
            // If there are no eligible entries, keep the previous winners as the new winners
            winners = new ArrayList<>(previousWinnerIds);
            LOGGER.info("No eligible entries found. Keeping the previous winners as the new winners.");
            event.reply("No eligible entries found. Keeping the previous winners as the new winners.").queue();
        } else if (eligibleEntries.size() <= winnersCount) {
            // If the number of eligible entries is smaller than the number of winners, make them the new winners
            winners = eligibleEntries;
        } else {
            // Otherwise, randomly select new winners
            winners = FairRandomizer.selectWinners(eligibleEntries, winnersCount);
        }

        // Deleting previous winners if there are new eligible entries
        if (!eligibleEntries.isEmpty()) {
            winnerService.deleteWinnersByGiveawayTitle(title);
        }
        LOGGER.info("Selected new winners for giveaway {}: {}", giveaway.getTitle(), winners);

        // Announcing the new winners
        StringBuilder winnerMessage = new StringBuilder("Reroll for giveaway " + giveaway.getTitle() + "! Congratulations to the new winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(giveaway.getTitle(), winnerId));
        }

        event.reply(winnerMessage.toString()).queue(); // here is the actual command that makes the announcement that appears in the channel
        LOGGER.info("Reroll for giveaway {} has ended!", giveaway.getTitle());
    }
}
