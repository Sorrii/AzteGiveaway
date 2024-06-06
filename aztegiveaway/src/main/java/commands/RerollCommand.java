/**
 * Class that handles the reroll command
 * This command is used to reroll a giveaway and select new winners
 * Only users with the ADMINISTRATOR permission can use this command
 * You can reroll a new number of winners using this command
 * If there are no eligible entries, the previous winners will be kept
 * If the number of eligible entries is smaller than the number of winners, they will be the new winners
 * USAGE: /giveaway reroll --giveaway_title "title" [--number_of_new_winners "number"]
 */

package commands;

import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

import org.example.utils.FairRandomizer;
import org.example.utils.LocalizationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RerollCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RerollCommand.class);
    private final GiveawayService giveawayService;
    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public RerollCommand(GiveawayService giveawayService, WinnerService winnerService, LocalizationUtil localizationUtil) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
        this.localizationUtil = localizationUtil;
    }

    @Transactional // Using this annotation to ensure that all db operations are done in a single transaction
    public void handleRerollCommand(SlashCommandInteractionEvent event) {
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
            LOGGER.warn("User {} does not have permission to use the reroll command", event.getUser());
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
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_required_options_reroll")).setEphemeral(true).queue();
            return;
        }

        // Retrieve the giveaway by title and initialize entries
        GiveawayEntity giveaway = giveawayService.getGiveawayByTitleAndGuildId(title, guildId);
        if (giveaway == null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_giveaway_found") + ": " + title).setEphemeral(true).queue();
            LOGGER.warn("Giveaway with title {} not found", title);
            return;
        }

        // Set the number of winners to the new number of winners if it's not null else set it to the original number of winners
        int winnersCount = newWinners != null ? newWinners : giveaway.getNumberOfWinners();

        // Retrieving previous winners to exclude them from the new draw
        List<WinnerEntity> previousWinners = winnerService.getWinnersByGiveawayMessageIdAndGuildId(giveaway.getMessageId(), guildId);
        Set<Long> previousWinnerIds = previousWinners.stream()
                .map(WinnerEntity::getUserId)
                .collect(Collectors.toSet());

        // Filtering out previous winners from the entries
        List<Long> eligibleEntries = giveawayService.getGiveawayEntries(giveaway.getId()).stream()
                .filter(entry -> !previousWinnerIds.contains(entry))
                .collect(Collectors.toList());

        // Check if there are any eligible entries left
        List<Long> winners;
        if (eligibleEntries.isEmpty()) {
            // If there are no eligible entries, keep the previous winners as the new winners
            winners = new ArrayList<>(previousWinnerIds);
            LOGGER.info("No eligible entries found. Keeping the previous winners as the new winners.");
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_eligible_entries")).queue();
        } else if (eligibleEntries.size() <= winnersCount) {
            // If the number of eligible entries is smaller than the number of winners, make them the new winners
            winners = eligibleEntries;
        } else {
            // Otherwise, randomly select new winners
            winners = FairRandomizer.selectWinners(eligibleEntries, winnersCount);
        }

        // Deleting previous winners if there are new eligible entries
        if (!eligibleEntries.isEmpty()) {
            LOGGER.info("Deleting previous winners for giveaway {}", giveaway.getTitle());
            winnerService.deleteWinnersByGiveawayMessageIdAndGuildId(giveaway.getMessageId(), guildId);
        }
        LOGGER.info("Selected new winners for giveaway {}: {}", giveaway.getTitle(), winners);

        // Announcing the new winners
        StringBuilder winnerMessage = new StringBuilder(localizationUtil.getLocalizedMessage(guildId, "reroll_success").replace("{0}", giveaway.getTitle()) + '\n');
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(giveaway.getTitle(), giveaway.getMessageId(), winnerId, guildId));
        }

        event.reply(winnerMessage.toString()).queue(); // here is the actual command that makes the announcement that appears in the channel
        LOGGER.info("Reroll for giveaway {} has ended!", giveaway.getTitle());
    }
}