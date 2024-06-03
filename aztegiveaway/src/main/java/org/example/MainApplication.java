/**
 * MainApplication that starts the Spring Boot application and the JDA bot and registers slash commands
 * It also retrieves and reschedules active giveaways
 */

package org.example;

import commands.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GiveawayUtil;

import java.time.Instant;
import java.util.List;

@SpringBootApplication(scanBasePackages = "org.example")
public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    @Value("${bot.token}")
    private String token;
    private final ApplicationContext context;

    public MainApplication(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @PostConstruct
    public void startBot() {
        if (token == null || token.isEmpty()) {
            LOGGER.error("Bot token is not set in config.properties");
            return;
        }

        try {
            // Get the services from the Spring context
            GiveawayService giveawayService = context.getBean(GiveawayService.class);
            WinnerService winnerService = context.getBean(WinnerService.class);

            // Initialize the commands
            GiveawayCommand giveawayCommand = new GiveawayCommand(giveawayService, winnerService);
            RerollCommand rerollCommand = new RerollCommand(giveawayService, winnerService);
            RollCommand rollCommand = new RollCommand(giveawayService, winnerService);
            WinnersCommand winnersCommand = new WinnersCommand(winnerService);
            DeleteCommand deleteCommand = new DeleteCommand(giveawayService);

            // Initialize the slash command listener
            SlashCommandListener slashCommandListener = new SlashCommandListener(giveawayCommand, rerollCommand, rollCommand, deleteCommand, winnersCommand);

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
                                            .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true)
                                    )
            ).queue();

            LOGGER.info("Bot is starting...");

            // Retrieve and reschedule active giveaways
            List<GiveawayEntity> activeGiveaways = giveawayService.getAllGiveaways();
            for (GiveawayEntity giveaway : activeGiveaways) {
                long remainingTime = giveaway.getDuration() - (Instant.now().toEpochMilli() - giveaway.getStartTime().toEpochMilli());
                if (remainingTime > 0) {
                    GiveawayUtil.scheduleGiveawayEnd(giveaway, jda, giveawayService, winnerService, remainingTime);
                    LOGGER.info("Rescheduled giveaway {} with remaining time {} ms", giveaway.getTitle(), remainingTime);
                } else {
                    // If the remaining time is negative or zero, end the giveaway immediately
                    GiveawayUtil.endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
                }
            }
        } catch (Exception e) {
            LOGGER.error("An unknown error occurred: ", e);
        }
    }
}
