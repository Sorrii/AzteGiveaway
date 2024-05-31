package commands;

/**
 * This class represents a command to create a giveaway.
 * The command is in the format: !giveaway create <title> <duration> <number_of_winners> [<channel_id>]
 */

import models.Giveaway;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GiveawayCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayCommand.class);
    private static final Map<Long, Giveaway> activeGiveaways = new HashMap<>();
    private static final Random RANDOM = new Random();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args.length > 1 && args[0].equalsIgnoreCase("!giveaway") && args[1].equalsIgnoreCase("create")) {
            handleCreateCommand(event, args);
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
                LOGGER.info("User with ID {} has already entered the giveaway entitled {}", event.getUserIdLong(), giveaway.getTitle());
                return;
            }
            giveaway.addEntry(event.getUserIdLong());
            LOGGER.info("User with ID {} entered the giveaway entitled{}", event.getUserIdLong(), giveaway.getTitle());
        } else {
            LOGGER.warn("No active giveaway found for message ID {}", messageId);
        }
    }

    public static void handleCreateCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 5) {
            event.getChannel().sendMessage("Usage: !giveaway create <title> <duration> <number_of_winners> [<channel_id>]").queue();
            return;
        }

        // We first check whether the user has the ADMINISTRATOR permission
        if (event.getMember() == null) {
            event.getChannel().sendMessage("You do not have permission to use that command!").queue();
            return;
        }

        List<Role> roles = event.getMember().getRoles();
        boolean isAdmin = roles.stream().anyMatch(role -> role.getPermissions().contains(net.dv8tion.jda.api.Permission.ADMINISTRATOR));

        // If the user is not an admin, we send a message and return
        if (!isAdmin) {
            event.getChannel().sendMessage("You do not have permission to use that command!").queue();
            return;
        }

        // Check if a giveaway with the same title already exists. Titles are unique so we can use them as identifiers
        String title = args[2];
        if (activeGiveaways.values().stream().anyMatch(g -> g.getTitle().equalsIgnoreCase(title))) {
            event.getChannel().sendMessage("A giveaway with this title already exists. Please choose a unique title.").queue();
            return;
        }

        String durationStr = args[3];
        int numberOfWinners;
        long channelId;
        TextChannel textChannel;

        // Ensure the number of winners is a positive NUMBER
        try {
            numberOfWinners = Integer.parseInt(args[4]);
            if (numberOfWinners <= 0) {
                event.getChannel().sendMessage("The number of winners must be a positive number.").queue();
                return;
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid number of winners. Please provide a valid positive number.").queue();
            return;
        }

        // Ensure the channel ID is a valid LONG
        if (args.length >= 6) {
            try {
                channelId = Long.parseLong(args[5]);
                // Check if the channel ID exists in the server
                textChannel = event.getGuild().getTextChannelById(channelId);
                if (textChannel == null) {
                    event.getChannel().sendMessage("The provided channel ID does not exist in this server.").queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid channel ID. Please provide a valid channel ID.").queue();
                return;
            }
        } else {
            textChannel = (TextChannel) event.getChannel();
        }

        // Validate duration format
        Duration duration;
        try {
            duration = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("Invalid duration format. Use formats like '1h', '30m', '2d'.").queue();
            return;
        }

        // Announce the giveaway in the specified channel
        textChannel.sendMessage("Giveaway created! Title: " + title + ", Duration: " + durationStr + ", Winners: " + numberOfWinners)
                .queue(message -> {
                    message.addReaction(Emoji.fromUnicode("🎉")).queue(); // Add a reaction to the message for users to enter
                    Giveaway giveaway = new Giveaway(message.getIdLong(), title, numberOfWinners, duration, textChannel.getIdLong());
                    activeGiveaways.put(message.getIdLong(), giveaway);
                    LOGGER.info("Giveaway with title: {} stored with message ID: {}", title, message.getIdLong());

                    // Schedule the giveaway end
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            endGiveaway(giveaway, message.getJDA());
                        }
                    }, duration.toMillis());
                });

        LOGGER.info("Giveaway created with title: {}, duration: {}, and winners: {}", title, durationStr, numberOfWinners);
    }

    private static Duration parseDuration(String durationStr) {
        if (durationStr.endsWith("h")) {
            long hours = Long.parseLong(durationStr.replace("h", ""));
            return Duration.of(hours, ChronoUnit.HOURS);
        } else if (durationStr.endsWith("m")) {
            long minutes = Long.parseLong(durationStr.replace("m", ""));
            return Duration.of(minutes, ChronoUnit.MINUTES);
        } else if (durationStr.endsWith("d")) {
            long days = Long.parseLong(durationStr.replace("d", ""));
            return Duration.of(days, ChronoUnit.DAYS);
        } else {
            throw new IllegalArgumentException("Invalid duration format");
        }
    }

    private static void endGiveaway(Giveaway giveaway, JDA jda) {
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

        List<Long> winners = new ArrayList<>();
        for (int i = 0; i < giveaway.getNumberOfWinners() && !entries.isEmpty(); i++) {
            int winnerIndex = RANDOM.nextInt(entries.size());
            winners.add(entries.remove(winnerIndex));
        }

        StringBuilder winnerMessage = new StringBuilder("The giveaway has ended! Congratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
        }

        textChannel.sendMessage(winnerMessage.toString()).queue();
        activeGiveaways.remove(giveaway.getMessageId());

        LOGGER.info("Giveaway {} has ended!", giveaway.getTitle());
    }
}
