package org.example;

import commands.GiveawayCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

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
            GiveawayService giveawayService = context.getBean(GiveawayService.class);
            WinnerService winnerService = context.getBean(WinnerService.class);
            GiveawayCommand giveawayCommand = new GiveawayCommand(giveawayService, winnerService);

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
                                            .addOption(OptionType.STRING, "prize", "The prize of the giveaway", true)
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
