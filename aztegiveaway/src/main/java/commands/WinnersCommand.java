/**
 * Class that handles the retrieval of winners for a certain giveaway
 * or for all giveaways in a guild.
 * Only users with the ADMINISTRATOR permission can use this command.
 * USAGE: /giveaway winners [--giveaway_title "title"] [--giveaway_message_id "messageId"]
 * If no giveaway title or message ID is provided, all winners for the guild will be retrieved.
 */

package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.example.entities.WinnerEntity;
import org.example.services.WinnerService;

import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.example.utils.EmbedUtil;
import org.example.utils.LocalizationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WinnersCommand extends ListenerAdapter { // it is not necessary to extend ListenerAdapter for pagination (see below)

    private static final Logger LOGGER = LoggerFactory.getLogger(WinnersCommand.class);
    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;
    private static final int NUMBER_OF_WINNERS_PER_PAGE = 5; // change the number of winners per page here

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

        // Get the giveaway title and message ID options and check if null
        final String title = Optional.ofNullable(event.getOption("giveaway_title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        final Long messageId = Optional.ofNullable(event.getOption("giveaway_message_id"))
                .map(OptionMapping::getAsLong)
                .orElse(null);


        // If no messageId/title is provided, retrieve all winners for the guild
        if (messageId == null && title == null) {
            List<WinnerEntity> winners = winnerService.getWinnersByGuildId(guildId);
            if (winners.isEmpty()) {
                event.reply(localizationUtil.getLocalizedMessage(guildId, "no_winners_found_all")).setEphemeral(true).queue();
                return;
            }

            List<String> winnerMessages = new ArrayList<>();
            for (WinnerEntity winner : winners) {
                winnerMessages.add("<@" + winner.getUserId() + "> (Giveaway: " + winner.getGiveawayTitle() + ")\n");
            }

            List<EmbedBuilder> embeds =
                    EmbedUtil.createPaginatedEmbeds(winnerMessages, NUMBER_OF_WINNERS_PER_PAGE); // 5 winners per page
            sendPaginatedEmbed(event, embeds);
            return;
        }

        // If a messageId and title are provided, retrieve winners for that specific giveaway
        if (messageId != null && title != null) {
            // Retrieving the winners by giveaway message ID and guild ID
            List<WinnerEntity> winners = winnerService.getWinnersByGiveawayMessageIdAndGuildId(messageId, guildId);
            LOGGER.info("Retrieved winners: {}", winners); // Add logging to check retrieved winners
            if (winners.isEmpty()) {
                event.reply(localizationUtil.getLocalizedMessage(guildId, "no_winners_found").replace("{0}", title)).setEphemeral(true).queue();
                return;
            }

            // Build the winner message
            StringBuilder winnerMessage = new StringBuilder(localizationUtil.getLocalizedMessage(guildId, "winners_list").replace("{0}", title) + "\n");
            for (WinnerEntity winner : winners) {
                winnerMessage.append("<@").append(winner.getUserId()).append(">\n");
            }

            event.reply(winnerMessage.toString()).queue();
            LOGGER.info("Winners for giveaway {}: {}", title, winnerMessage);
        } else {
            LOGGER.warn("Missing required options for winners command.");
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_required_options_winners_command")).setEphemeral(true).queue();
        }
    }

    // Method to send a paginated embed
    // It embeds 5 winners per page and adds reaction buttons to navigate through the pages
    // The method listens for reaction events and updates the embed accordingly (that's why it extends ListenerAdapter)
    private void sendPaginatedEmbed(SlashCommandInteractionEvent event, List<EmbedBuilder> embeds) {
        if (embeds.isEmpty()) {
            LOGGER.warn("No embeds to send.");
            return;
        }

        event.replyEmbeds(embeds.get(0).build()).queue(response -> {
            if (embeds.size() > 1) {
                response.retrieveOriginal().queue(message -> {
                    message.addReaction(Emoji.fromUnicode("⬅️")).queue();
                    message.addReaction(Emoji.fromUnicode("➡️")).queue();
                    handleReactions(event, message, embeds);
                });
            }
        });
    }

    private void handleReactions(SlashCommandInteractionEvent event, Message message, List<EmbedBuilder> embeds) {
        final int[] currentPage = {0};

        event.getJDA().addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReactionAdd(@NotNull MessageReactionAddEvent reactionEvent) {
                // Ignore bot reactions
                if (reactionEvent.getUser() != null && reactionEvent.getUser().isBot()) {
                    return;
                }

                if (reactionEvent.getMessageIdLong() != message.getIdLong()) return;

                reactionEvent.getReaction().removeReaction(reactionEvent.getUser()).queue();

                if (reactionEvent.getReaction().getEmoji().getName().equals("⬅️")) {
                    if (currentPage[0] > 0) {
                        currentPage[0]--;
                        message.editMessageEmbeds(embeds.get(currentPage[0]).build()).queue();
                    }
                } else if (reactionEvent.getReaction().getEmoji().getName().equals("➡️")) {
                    if (currentPage[0] < embeds.size() - 1) {
                        currentPage[0]++;
                        message.editMessageEmbeds(embeds.get(currentPage[0]).build()).queue();
                    }
                }
            }
        });
    }
}