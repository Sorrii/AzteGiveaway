package commands;

import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;

import net.dv8tion.jda.api.JDA;
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

import utils.FairRandomizer;
import utils.DurationParser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

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

    private void handleCreateCommand(SlashCommandInteractionEvent event) {
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

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use that command!").setEphemeral(true).queue();
            return;
        }

        // Check if a giveaway with the same title already exists. Titles are unique so we can use them as identifiers
        if (giveawayService.getGiveawayByTitle(title) != null) {
            event.reply("A giveaway with this title already exists. Please choose a unique title.").setEphemeral(true).queue();
            return;
        }

        // Create the giveaway
        textChannel.sendMessage("Giveaway created! Title: " + title + ", Prize: " + prize + ", Duration: " + durationStr + ", Number of winners: " + numberOfWinners)
                .queue(message -> {
                    message.addReaction(Emoji.fromUnicode("🎉")).queue();
                    GiveawayEntity giveaway = new GiveawayEntity(message.getIdLong(), title, numberOfWinners, DurationParser.parseDuration(durationStr), textChannel.getIdLong());
                    giveawayService.createGiveaway(giveaway);

                    // Schedule the giveaway end
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            endGiveaway(giveaway, message.getJDA());
                        }
                    }, DurationParser.parseDuration(durationStr));
                });

        event.reply("Giveaway created successfully!").setEphemeral(true).queue();
        LOGGER.info("Giveaway created with title: {}, duration: {}, and winners: {}", title, durationStr, numberOfWinners);
    }

    private void endGiveaway(GiveawayEntity giveaway, JDA jda) {
        giveaway = giveawayService.getGiveawayByMessageId(giveaway.getMessageId()); // reload with updated entries
        LOGGER.info("Entries: {}", giveaway.getEntries());

        List<Long> winners = FairRandomizer.selectWinners(giveaway.getEntries(), giveaway.getNumberOfWinners());
        LOGGER.info("Selected winners for giveaway {}: {}", giveaway.getEntries(), winners);

        TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            LOGGER.error("Channel not found for giveaway: {}", giveaway.getMessageId());
            return;
        }

        if (winners.isEmpty()) {
            textChannel.sendMessage("The giveaway has ended, but there were no entries.").queue();
            giveawayService.deleteGiveaway(giveaway.getId());
            return;
        }

        StringBuilder winnerMessage = new StringBuilder("The giveaway has ended! Congratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(giveaway.getTitle(), winnerId));
        }

        textChannel.sendMessage(winnerMessage.toString()).queue();
        giveawayService.deleteGiveaway(giveaway.getId());

        LOGGER.info("Giveaway {} has ended!", giveaway.getTitle());
    }
}
