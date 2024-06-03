/**
 * Class that handles the giveaway command
 * This command is used to create a new giveaway
 * Only users with the ADMINISTRATOR permission can use this command
 * USAGE: /giveaway create --title "title" --prize "prize" --duration "duration" --winners "winners" [--channel "channel"] <- optional param
 */

package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import org.example.entities.GiveawayEntity;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import utils.DurationParser;
import utils.EmbedUtil;
import utils.GiveawayUtil;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
public class GiveawayCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayCommand.class);

    private final GiveawayService giveawayService;
    private final WinnerService winnerService;

    @Autowired
    public GiveawayCommand(GiveawayService giveawayService, WinnerService winnerService) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() != null && event.getUser().isBot()) {
            return; // Ignore bot reactions
        }

        long messageId = event.getMessageIdLong();
        GiveawayEntity giveaway = giveawayService.getGiveawayByMessageId(messageId);

        if (giveaway != null) {
            long userId = event.getUserIdLong();
            if (!giveaway.getEntries().contains(userId)) {
                giveaway.addEntry(userId);
                giveawayService.updateGiveaway(giveaway);  // Update the giveaway with the new entry
                LOGGER.info("User with ID {} entered the giveaway with title: {}", userId, giveaway.getTitle());
            } else {
                LOGGER.info("User with ID {} has already entered the giveaway with title: {}", userId, giveaway.getTitle());
            }

            LOGGER.info("Entries for giveaway: {}", giveaway.getEntries());
        }
    }

    public void handleCreateCommand(SlashCommandInteractionEvent event) {
        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use that command!").setEphemeral(true).queue();
            return;
        }

        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("title"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        final String prize = Optional.ofNullable(event.getOption("prize"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        final String durationStr = Optional.ofNullable(event.getOption("duration"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        final Integer numberOfWinners = Optional.ofNullable(event.getOption("winners"))
                .map(OptionMapping::getAsInt)
                .orElse(null);

        // Initialize textChannel to null initially
        final TextChannel textChannel;

        if (event.getOption("channel") != null) {
            textChannel = Objects.requireNonNull(event.getOption("channel")).getAsChannel().asTextChannel();
        } else {
            textChannel = (TextChannel) event.getChannel();
        }

        // Ensure all required options are present
        if (title == null || prize == null || durationStr == null || numberOfWinners == null) {
            event.reply("Missing required options: title, prize, duration, or winners.").setEphemeral(true).queue();
            return;
        }

        // Check if a giveaway with the same title already exists. Titles are unique so we can use them as identifiers
        if (giveawayService.getGiveawayByTitle(title) != null) {
            event.reply("A giveaway with this title already exists. Please choose a unique title.").setEphemeral(true).queue();
            return;
        }

        // Calculate end time
        long durationMillis = DurationParser.parseDuration(durationStr);
        Instant endTime = Instant.now().plusMillis(durationMillis);

        // Create the giveaway embed
        EmbedBuilder embedBuilder = EmbedUtil.createGiveawayEmbed(title, prize, durationStr, numberOfWinners, endTime);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
            message.addReaction(Emoji.fromUnicode("🎉")).queue();
            GiveawayEntity giveaway = new GiveawayEntity(message.getIdLong(), title, numberOfWinners, durationMillis, textChannel.getIdLong());
            giveawayService.createGiveaway(giveaway);

            // Schedule the giveaway end
            GiveawayUtil.scheduleGiveawayEnd(giveaway, message.getJDA(), giveawayService, winnerService, durationMillis);
        });

        event.reply("Giveaway created successfully!").setEphemeral(true).queue();
        LOGGER.info("Giveaway created with title: {}, duration: {}, and winners: {}", title, durationStr, numberOfWinners);
    }
}
