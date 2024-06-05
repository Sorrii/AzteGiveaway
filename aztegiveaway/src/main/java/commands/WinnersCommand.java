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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.example.utils.LocalizationUtil;

import java.util.List;
import java.util.Optional;

@Component
public class WinnersCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinnersCommand.class);

    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public WinnersCommand(WinnerService winnerService, LocalizationUtil localizationUtil) {
        this.winnerService = winnerService;
        this.localizationUtil = localizationUtil;
    }

    public void handleWinnersCommand(SlashCommandInteractionEvent event) {
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
            LOGGER.warn("User {} does not have permission to use the winners command", event.getUser());
            return;
        }

        // Get the giveaway title option and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        // Check if the title is null
        if (title == null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_title")).setEphemeral(true).queue();
            return;
        }

        // Retrieving the winners by giveaway title
        List<WinnerEntity> winners = winnerService.getWinnersByGiveawayTitle(title);
        if (winners.isEmpty()) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_winners_found").replace("{0}", title)).setEphemeral(true).queue();
            return;
        }

        StringBuilder winnerMessage = new StringBuilder();
        for (WinnerEntity winner : winners) {
            winnerMessage.append("<@").append(winner.getUserId()).append(">\n");
        }

        event.reply(localizationUtil.getLocalizedMessage(guildId, "winners_list")
                .replace("{0}", title)
                .replace("{1}", winnerMessage.toString())).queue();

        LOGGER.info("Winners for giveaway {}: {}", title, winners);
    }
}

