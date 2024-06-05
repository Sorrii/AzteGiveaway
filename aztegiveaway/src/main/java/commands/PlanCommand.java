package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.utils.DurationParser;
import org.example.utils.EmbedUtil;
import org.example.utils.GiveawayUtil;
import org.example.utils.LocalizationUtil;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class PlanCommand extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanCommand.class);

    private final GiveawayService giveawayService;
    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public PlanCommand(GiveawayService giveawayService, WinnerService winnerService, LocalizationUtil localizationUtil) {
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
        this.localizationUtil = localizationUtil;
    }

    public void handlePlanCommand(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();

        // Ensure the user has the ADMINISTRATOR permission
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            LOGGER.warn("User {} does not have permission to use the giveaway command", event.getUser());
            event.reply(localizationUtil.getLocalizedMessage(guildId, "no_permission")).setEphemeral(true).queue();
            return;
        }

        final String startTimeStr = Optional.ofNullable(event.getOption("start_time"))
                .map(OptionMapping::getAsString)
                .orElse(null);

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
        if (title == null || prize == null || startTimeStr == null || durationStr == null || numberOfWinners == null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "missing_required_options_plan")).setEphemeral(true).queue();
            return;
        }

        if (giveawayService.getGiveawayByTitleAndGuildId(title, guildId) != null) {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "giveaway_exists")).setEphemeral(true).queue();
            LOGGER.warn("Giveaway with title {} already exists. Creation failed!", title);
            return;
        }

        // Parse start time and duration
        long startTimeMillis = DurationParser.parseDuration(startTimeStr);
        long durationMillis = DurationParser.parseDuration(durationStr);
        Instant startTime = Instant.now().plusMillis(startTimeMillis);

        // Create and save a new GiveawayEntity with messageId set to 0L initially
        GiveawayEntity giveaway = new GiveawayEntity();
        giveaway.setTitle(title);
        giveaway.setPrize(prize);
        giveaway.setStartTime(startTime);
        giveaway.setDuration(durationMillis);
        giveaway.setNumberOfWinners(numberOfWinners);
        giveaway.setChannelId(textChannel.getIdLong());
        giveaway.setMessageId(0L);
        giveaway.setGuildId(guildId);

        giveawayService.createGiveaway(giveaway);
        GiveawayUtil giveawayUtil = new GiveawayUtil(localizationUtil);

        // Schedule the task
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                EmbedBuilder embedBuilder =
                        EmbedUtil.createGiveawayEmbed(
                                title,
                                prize,
                                durationStr,
                                numberOfWinners,
                                Instant.now().plusMillis(durationMillis),
                                guildId,
                                localizationUtil
                        );

                textChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                    message.addReaction(Emoji.fromUnicode("🎉")).queue();
                    giveaway.setMessageId(message.getIdLong());
                    giveawayService.updateGiveaway(giveaway);
                    giveawayUtil.scheduleGiveawayEnd(giveaway, message.getJDA(), giveawayService, winnerService, durationMillis);
                });
            }
        }, startTimeMillis);

        event.reply(localizationUtil.getLocalizedMessage(guildId, "giveaway_scheduled_success")).setEphemeral(true).queue();
        LOGGER.info("Giveaway scheduled with title: {}, start time: {}, duration: {}, and winners: {}", title, startTimeStr, durationStr, numberOfWinners);
    }
}