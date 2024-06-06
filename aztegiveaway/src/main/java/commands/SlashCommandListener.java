/**
 * Class that handles the slash commands
 * Listener for slash commands
 * This class is responsible for handling the slash commands and routing them to the appropriate command handler
 */

package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.example.utils.LocalizationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class SlashCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    private final Map<String, Consumer<SlashCommandInteractionEvent>> commandHandlers = new HashMap<>();
    private final Map<String, Consumer<SlashCommandInteractionEvent>> setCommandHandlers = new HashMap<>();
    private final LocalizationUtil localizationUtil;

    @Autowired
    public SlashCommandListener(GiveawayCommand giveawayCommand,
                                RerollCommand rerollCommand,
                                RollCommand rollCommand,
                                DeleteCommand deleteCommand,
                                WinnersCommand winnersCommand,
                                PlanCommand planCommand,
                                SetLanguageCommand setLanguageCommand,
                                LocalizationUtil localizationUtil) {

        this.localizationUtil = localizationUtil;

        // Initialize command handlers
        commandHandlers.put("create", event -> {
            LOGGER.info("Set create command");
            giveawayCommand.handleCreateCommand(event);
        });
        commandHandlers.put("reroll", event -> {
            LOGGER.info("Set reroll command");
            rerollCommand.handleRerollCommand(event);
        });
        commandHandlers.put("roll", event -> {
            LOGGER.info("Set roll command");
            rollCommand.handleRollCommand(event);
        });
        commandHandlers.put("delete", event -> {
            LOGGER.info("Set delete command");
            deleteCommand.handleDeleteCommand(event);
        });
        commandHandlers.put("winners", event -> {
            LOGGER.info("Set winners command");
            winnersCommand.handleWinnersCommand(event);
        });
        commandHandlers.put("plan", event -> {
            LOGGER.info("Set plan command");
            planCommand.handlePlanCommand(event);
        });

        // Initialize set command handlers
        setCommandHandlers.put("language", event -> {
            LOGGER.info("Set set language command");
            setLanguageCommand.handleSetLanguageCommand(event);
        });
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Ensure the guild is not null
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();

        // Handle 'giveaway' commands
        if ("giveaway".equals(commandName) && subcommandName != null) {
            handleCommand(event, commandHandlers, subcommandName, guildId);
        }
        // Handle 'set' commands
        else if ("set".equals(commandName) && subcommandName != null) {
            handleCommand(event, setCommandHandlers, subcommandName, guildId);
        }
        // Unknown command
        else {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "unknown_command").replace("{0}", commandName)).setEphemeral(true).queue();
        }
    }

    private void handleCommand(SlashCommandInteractionEvent event, Map<String, Consumer<SlashCommandInteractionEvent>> handlers, String subcommandName, Long guildId) {
        Consumer<SlashCommandInteractionEvent> handler = handlers.get(subcommandName);
        if (handler != null) {
            LOGGER.info("Received subcommand: {}", subcommandName);
            handler.accept(event);
        } else {
            event.reply(localizationUtil.getLocalizedMessage(guildId, "unknown_subcommand").replace("{0}", subcommandName)).setEphemeral(true).queue();
        }
    }
}
