package commands;

import models.Giveaway;
import utils.FairRandomizer;
import utils.DurationParser;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GiveawayCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayCommand.class);
    private static final Map<Long, Giveaway> activeGiveaways = new ConcurrentHashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("giveaway".equals(event.getName())) {
            String subcommand = event.getSubcommandName();
            if (Objects.equals(subcommand, "create")) {
                handleCreateCommand(event);
            } else {
                event.reply("Unknown subcommand: " + subcommand).setEphemeral(true).queue();
            }
        } else {
            event.reply("Unknown command: " + event.getName()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        LOGGER.info("Reaction added by user: {}", event.getUserIdLong());

        if (event.getUser() != null && event.getUser().isBot()) {
            LOGGER.info("Ignored bot reaction");
            return;
        }

        long messageId = event.getMessageIdLong();
        LOGGER.info("User with ID {} reacted to message ID {}", event.getUserIdLong(), messageId);
        if (activeGiveaways.containsKey(messageId)) {
            Giveaway giveaway = activeGiveaways.get(messageId);
            if (giveaway.getEntries().contains(event.getUserIdLong())) {
                LOGGER.info("User with ID {} has already entered the giveaway titled {}", event.getUserIdLong(), giveaway.getTitle());
                return;
            }
            giveaway.addEntry(event.getUserIdLong());
            LOGGER.info("User with ID {} entered the giveaway titled {}", event.getUserIdLong(), giveaway.getTitle());
        } else {
            LOGGER.warn("No active giveaway found for message ID {}", messageId);
        }
    }

    private void handleCreateCommand(SlashCommandInteractionEvent event) {
        // Get the options and check if null
        final String title = Optional.ofNullable(event.getOption("title"))
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
        if (title == null || durationStr == null || numberOfWinners == null) {
            event.reply("Missing required options: title, duration, or winners.").setEphemeral(true).queue();
            return;
        }

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use that command!").setEphemeral(true).queue();
            return;
        }

        // Check if a giveaway with the same title already exists. Titles are unique so we can use them as identifiers
        if (activeGiveaways.values().stream().anyMatch(g -> g.getTitle().equalsIgnoreCase(title))) {
            event.reply("A giveaway with this title already exists. Please choose a unique title.").setEphemeral(true).queue();
            return;
        }

        // Validate duration format
        long duration;
        try {
            duration = DurationParser.parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            event.reply("Invalid duration format. Use formats like '1h', '30m', '2d'.").setEphemeral(true).queue();
            return;
        }

        // Announce the giveaway in the specified channel
        textChannel.sendMessage("Giveaway created! Title: " + title + ", Duration: " + durationStr + ", Number of winners: " + numberOfWinners)
                .queue(message -> {
                    message.addReaction(Emoji.fromUnicode("🎉")).queue();
                    Giveaway giveaway = new Giveaway(message.getIdLong(), title, numberOfWinners, duration, textChannel.getIdLong());
                    activeGiveaways.put(message.getIdLong(), giveaway);
                    LOGGER.info("Giveaway with title: {} stored with message ID: {}", title, message.getIdLong());

                    // Schedule the giveaway end
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            endGiveaway(giveaway, message.getJDA());
                        }
                    }, duration);
                });

        event.reply("Giveaway created successfully!").setEphemeral(true).queue();
        LOGGER.info("Giveaway created with title: {}, duration: {}, and winners: {}", title, durationStr, numberOfWinners);
    }

    private void endGiveaway(Giveaway giveaway, JDA jda) {
        TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            LOGGER.error("Channel not found for giveaway: {}", giveaway.getMessageId());
            return;
        }

        List<Long> entries = giveaway.getEntries();
        if (entries.isEmpty()) {
            textChannel.sendMessage("The giveaway has ended, but there were no entries.").queue();
            return;
        }

        List<Long> winners = FairRandomizer.selectWinners(entries, giveaway.getNumberOfWinners());

        StringBuilder winnerMessage = new StringBuilder("The giveaway has ended! Congratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
        }

        textChannel.sendMessage(winnerMessage.toString()).queue();
        activeGiveaways.remove(giveaway.getMessageId());

        LOGGER.info("Giveaway {} has ended!", giveaway.getTitle());
    }
}
