/**
 * This class listens for slash commands and delegates the handling of the commands to the appropriate command classes
 * The command classes are autowired into this class and are responsible for handling the commands
 */

package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public SlashCommandListener(GiveawayCommand giveawayCommand,
                                RerollCommand rerollCommand,
                                RollCommand rollCommand,
                                DeleteCommand deleteCommand,
                                WinnersCommand winnersCommand,
                                PlanCommand planCommand) {

        this.giveawayCommand = giveawayCommand;
        this.rerollCommand = rerollCommand;
        this.rollCommand = rollCommand;
        this.deleteCommand = deleteCommand;
        this.winnersCommand = winnersCommand;
        this.planCommand = planCommand;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
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
                event.reply("Unknown subcommand: " + subcommand).setEphemeral(true).queue();
            }
        } else {
            event.reply("Unknown command: " + event.getName()).setEphemeral(true).queue();
        }
    }
}
