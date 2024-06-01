package org.example;

import commands.GiveawayCommand;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);
    private static String token;

    public static void main(String[] args) {
        loadConfig();
        new MainApplication().startBot();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = MainApplication.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.error("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
            token = properties.getProperty("bot.token");
        } catch (IOException ex) {
            LOGGER.error("Error loading config.properties", ex);
        }
    }

    public void startBot() {
        if (token == null || token.isEmpty()) {
            LOGGER.error("Bot token is not set in config.properties");
            return;
        }

        try {
            GiveawayCommand giveawayCommand = new GiveawayCommand();

            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    )
                    .addEventListeners(giveawayCommand)
                    .build();

            // Register slash commands
            jda.updateCommands().addCommands(
                    Commands.slash("giveaway", "Manage giveaways")
                            .addSubcommands(
                                    new SubcommandData("create", "Create a new giveaway")
                                            .addOption(OptionType.STRING, "title", "The title of the giveaway", true)
                                            .addOption(OptionType.STRING, "duration", "The duration of the giveaway (e.g., 30m, 1h, 2d)", true)
                                            .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                                            .addOption(OptionType.CHANNEL, "channel", "The channel where the giveaway will be announced", false)
                            )
            ).queue();

            LOGGER.info("Bot is starting...");
        } catch (Exception e) {
            LOGGER.error("An unknown error occurred: ", e);
        }
    }
}
