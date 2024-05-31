package org.example;

import commands.GiveawayCommand;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Properties config = loadConfig();
        String token = config.getProperty("bot.token");

        if (token == null || token.isEmpty()) {
            LOGGER.error("Bot token is not set in config.properties");
            return;
        }

        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    )
                    .addEventListeners(new GiveawayCommand())
                    .build();
            LOGGER.info("Bot is starting...");
        } catch (Exception e) {
            LOGGER.error("An unknown error occurred: ", e);
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.error("Sorry, unable to find config.properties");
                return props;
            }
            props.load(input);
        } catch (IOException ex) {
            LOGGER.error("IOException occurred while loading config properties", ex);
        }
        return props;
    }
}
