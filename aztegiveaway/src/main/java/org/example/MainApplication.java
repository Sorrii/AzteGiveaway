package org.example;

import commands.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import org.example.services.GiveawayService;
import org.example.services.LanguagePreferenceService;
import org.example.services.WinnerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.utils.LocalizationUtil;
import org.example.utils.RescheduleUtil;
import org.example.utils.SlashCommandRegistrationUtil;

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
            GiveawayService giveawayService = context.getBean(GiveawayService.class);
            WinnerService winnerService = context.getBean(WinnerService.class);
            LanguagePreferenceService languagePreferenceService = context.getBean(LanguagePreferenceService.class);

            GiveawayCommand giveawayCommand = new GiveawayCommand(giveawayService, winnerService, localizationUtil);
            RerollCommand rerollCommand = new RerollCommand(giveawayService, winnerService, localizationUtil);
            RollCommand rollCommand = new RollCommand(giveawayService, winnerService, localizationUtil);
            WinnersCommand winnersCommand = new WinnersCommand(winnerService, localizationUtil);
            DeleteCommand deleteCommand = new DeleteCommand(giveawayService, localizationUtil);
            PlanCommand planCommand = new PlanCommand(giveawayService, winnerService, localizationUtil);
            SetLanguageCommand setLanguageCommand = new SetLanguageCommand(languagePreferenceService, localizationUtil);

            SlashCommandListener slashCommandListener = new SlashCommandListener(
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
                    .addEventListeners(slashCommandListener, giveawayCommand)
                    .addEventListeners(giveawayCommand)
                    .build();

            SlashCommandRegistrationUtil.registerSlashCommands(jda);

            LOGGER.info("Bot is starting...");

            RescheduleUtil rescheduleUtil = new RescheduleUtil(jda, giveawayService, winnerService, localizationUtil);
            rescheduleUtil.rescheduleActiveAndPlannedGiveaways();
        } catch (Exception e) {
            LOGGER.error("An unknown error occurred: ", e);
        }
    }
}
