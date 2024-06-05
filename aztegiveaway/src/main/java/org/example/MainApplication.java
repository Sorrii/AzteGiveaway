/**
 * MainApplication that starts the Spring Boot application and the JDA bot and registers slash commands
 * It also retrieves and reschedules active giveaways
 */

package org.example;

import commands.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;
import org.example.services.LanguagePreferenceService;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.utils.EmbedUtil;
import org.example.utils.GiveawayUtil;
import org.example.utils.LocalizationUtil;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication(scanBasePackages = "org.example")
public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    @Value("${bot.token}")
    private String token;
    private final ApplicationContext context;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public MainApplication(ApplicationContext context, LocalizationUtil localizationUtil) {
        this.context = context;
        this.localizationUtil = localizationUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @PostConstruct
    public void startBot() {
        if (token == null || token.isEmpty()) {
            LOGGER.error("Bot token is not set in application.properties");
            return;
        }

        try {
            // Get the services from the Spring context
            GiveawayService giveawayService = context.getBean(GiveawayService.class);
            WinnerService winnerService = context.getBean(WinnerService.class);
            LanguagePreferenceService languagePreferenceService = context.getBean(LanguagePreferenceService.class);

            // Initialize the commands
            GiveawayCommand giveawayCommand = new GiveawayCommand(giveawayService, winnerService, localizationUtil);
            RerollCommand rerollCommand = new RerollCommand(giveawayService, winnerService, localizationUtil);
            RollCommand rollCommand = new RollCommand(giveawayService, winnerService, localizationUtil);
            WinnersCommand winnersCommand = new WinnersCommand(winnerService, localizationUtil);
            DeleteCommand deleteCommand = new DeleteCommand(giveawayService, localizationUtil);
            PlanCommand planCommand = new PlanCommand(giveawayService, winnerService, localizationUtil);
            SetLanguageCommand setLanguageCommand = new SetLanguageCommand(languagePreferenceService, localizationUtil);

            // Initialize the slash command listener
            SlashCommandListener slashCommandListener =
                    new SlashCommandListener(
                            giveawayCommand,
                            rerollCommand,
                            rollCommand,
                            deleteCommand,
                            winnersCommand,
                            planCommand,
                            setLanguageCommand,
                            localizationUtil
                    );

            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    )
                    .addEventListeners(slashCommandListener)
                    .addEventListeners(giveawayCommand)
                    .build();

            // Register slash commands
            jda.updateCommands().addCommands(
                    Commands.slash("giveaway", "Manage giveaways")
                            .addSubcommands(
                                    new SubcommandData("create", "Create a new giveaway")
                                            .addOption(OptionType.STRING, "title", "The title of the giveaway", true)
                                            .addOption(OptionType.STRING, "prize", "The prize of the giveaway", true)
                                            .addOption(OptionType.STRING, "duration", "The duration of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                                            .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                                            .addOption(OptionType.CHANNEL, "channel", "The channel where the giveaway will be announced", false),
                                    new SubcommandData("reroll", "Reroll the giveaway")
                                            .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true)
                                            .addOption(OptionType.INTEGER, "number_of_new_winners", "The number of new winners", false),
                                    new SubcommandData("roll", "Roll the giveaway immediately")
                                            .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true),
                                    new SubcommandData("delete", "Delete the giveaway")
                                            .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true),
                                    new SubcommandData("winners", "Get the winners of the giveaway")
                                            .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", false)
                                            .addOption(OptionType.STRING, "giveaway_message_id", "The message ID of the giveaway", false),
                                    new SubcommandData("plan", "Schedule a new giveaway")
                                            .addOption(OptionType.STRING, "start_time", "The start time of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                                            .addOption(OptionType.STRING, "title", "The title of the giveaway", true)
                                            .addOption(OptionType.STRING, "prize", "The prize of the giveaway", true)
                                            .addOption(OptionType.STRING, "duration", "The duration of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                                            .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                                            .addOption(OptionType.CHANNEL, "channel", "The channel where the giveaway will be announced", false)
                            ),
                    Commands.slash("set", "Set the language preference for the bot")
                            .addSubcommands(
                                    new SubcommandData("language", "Set the language preference for the bot")
                                            .addOptions(
                                                    new OptionData(OptionType.STRING, "language", "The language preference for the bot")
                                                            .addChoice("English", "en")
                                                            .addChoice("Romanian", "ro")
                                            )

                            )
            ).queue();

            LOGGER.info("Bot is starting...");

            // Retrieve and reschedule active and planned giveaways
            List<GiveawayEntity> activeGiveaways = giveawayService.getAllGiveaways();
            GiveawayUtil giveawayUtil = new GiveawayUtil(localizationUtil);
            for (GiveawayEntity giveaway : activeGiveaways) {
                long currentTime = Instant.now().toEpochMilli();
                long startTime = giveaway.getStartTime().toEpochMilli();
                long remainingTime = giveaway.getDuration() - (currentTime - startTime);

                if (startTime > currentTime) {
                    // Giveaway is planned for future
                    new Timer().schedule(new TimerTask() {
                        final TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
                        // DO NOT DELETE THE CHANNEL BEFORE THE GIVEAWAY ENDS PLS
                        @Override
                        public void run() {
                            assert textChannel != null; // hopefully it's never null :)
                            EmbedBuilder embedBuilder = EmbedUtil.createGiveawayEmbed(
                                    giveaway.getTitle(),
                                    giveaway.getPrize(),
                                    giveaway.getDuration() / 1000 + "s",
                                    giveaway.getNumberOfWinners(),
                                    Instant.now().plusMillis(giveaway.getDuration()),
                                    giveaway.getGuildId(),
                                    localizationUtil
                            );

                            textChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                                message.addReaction(Emoji.fromUnicode("🎉")).queue();
                                giveaway.setMessageId(message.getIdLong());
                                giveawayService.updateGiveaway(giveaway);
                                giveawayUtil.scheduleGiveawayEnd(giveaway, jda, giveawayService, winnerService, giveaway.getDuration());
                            });
                        }
                    }, startTime - currentTime);
                    LOGGER.info("Scheduled future giveaway {} to start at {}", giveaway.getTitle(), giveaway.getStartTime());
                } else if (remainingTime > 0) {
                    // Giveaway is active
                    giveawayUtil.scheduleGiveawayEnd(giveaway, jda, giveawayService, winnerService, remainingTime);
                    LOGGER.info("Rescheduled giveaway {} with remaining time {} ms", giveaway.getTitle(), remainingTime);
                } else {
                    // If the remaining time is negative or zero, end the giveaway immediately
                    giveawayUtil.endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
                }
            }
        } catch (Exception e) {
            LOGGER.error("An unknown error occurred: ", e);
        }
    }
}