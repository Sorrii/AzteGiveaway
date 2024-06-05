package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.utils.LocalizationUtil;

import java.util.Objects;

@Component
public class SlashCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    private final GiveawayCommand giveawayCommand;
    private final RerollCommand rerollCommand;
    private final RollCommand rollCommand;
    private final DeleteCommand deleteCommand;
    private final WinnersCommand winnersCommand;
    private final PlanCommand planCommand;
    private final SetLanguageCommand setLanguageCommand;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public SlashCommandListener(GiveawayCommand giveawayCommand,
                                RerollCommand rerollCommand,
                                RollCommand rollCommand,
                                DeleteCommand deleteCommand,
                                WinnersCommand winnersCommand,
                                PlanCommand planCommand,
                                SetLanguageCommand setLanguageCommand,
                                LocalizationUtil localizationUtil
                                ) {

        this.giveawayCommand = giveawayCommand;
        this.rerollCommand = rerollCommand;
        this.rollCommand = rollCommand;
        this.deleteCommand = deleteCommand;
        this.winnersCommand = winnersCommand;
        this.planCommand = planCommand;
        this.setLanguageCommand = setLanguageCommand;
        this.localizationUtil = localizationUtil;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();


        // Command handling block
        if ("giveaway".equals(event.getName())) {
            String subcommand = event.getSubcommandName();
            if (Objects.equals(subcommand, "create")) {
                LOGGER.info("Received create command");
                giveawayCommand.handleCreateCommand(event);
            } else if (Objects.equals(subcommand, "reroll")) {
                LOGGER.info("Received reroll command");
                rerollCommand.handleRerollCommand(event);
            } else if (Objects.equals(subcommand, "roll")) {
                LOGGER.info("Received roll command");
                rollCommand.handleRollCommand(event);
            } else if (Objects.equals(subcommand, "delete")) {
                LOGGER.info("Received delete command");
                deleteCommand.handleDeleteCommand(event);
            } else if (Objects.equals(subcommand, "winners")) {
                LOGGER.info("Received winners command");
                winnersCommand.handleWinnersCommand(event);
            } else if (Objects.equals(subcommand, "plan")) {
                LOGGER.info("Received plan command");
                planCommand.handlePlanCommand(event);
            } else {
                event.reply(localizationUtil.getLocalizedMessage(guildId, "unknown_subcommand").replace("{0}", event.getName())).setEphemeral(true).queue();
            }
        } else if ("setlanguage".equals(event.getName())) {
            LOGGER.info("Received setlanguage command");
            setLanguageCommand.handleSetLanguageCommand(event);
        } else {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "unknown_command").replace("{0}", event.getName())).setEphemeral(true).queue();
        }
    }
}
