/**
 * This class is responsible for registering the slash commands for the bot.
 * If new command needs to be added, simply create a new method that returns a SubcommandData object
 */

package org.example.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class SlashCommandRegistrationUtil {

    public static void registerSlashCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("giveaway", "Manage giveaways")
                        .addSubcommands(
                                createGiveawaySubcommand(),
                                createRerollSubcommand(),
                                createRollSubcommand(),
                                createDeleteSubcommand(),
                                createWinnersSubcommand(),
                                createPlanSubcommand()
                        ),
                Commands.slash("set", "Set the language preference for the bot")
                        .addSubcommands(
                                createSetLanguageSubcommand()
                        )
        ).queue();
    }

    private static SubcommandData createGiveawaySubcommand() {
        return new SubcommandData("create", "Create a new giveaway")
                .addOption(OptionType.STRING, "title", "The title of the giveaway", true)
                .addOption(OptionType.STRING, "prize", "The prize of the giveaway", true)
                .addOption(OptionType.STRING, "duration", "The duration of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                .addOption(OptionType.CHANNEL, "channel", "The channel where the giveaway will be announced", false);
    }

    private static SubcommandData createRerollSubcommand() {
        return new SubcommandData("reroll", "Reroll the giveaway")
                .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true)
                .addOption(OptionType.INTEGER, "number_of_new_winners", "The number of new winners", false);
    }

    private static SubcommandData createRollSubcommand() {
        return new SubcommandData("roll", "Roll the giveaway immediately")
                .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true);
    }

    private static SubcommandData createDeleteSubcommand() {
        return new SubcommandData("delete", "Delete the giveaway")
                .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", true);
    }

    private static SubcommandData createWinnersSubcommand() {
        return new SubcommandData("winners", "Get the winners of the giveaway")
                .addOption(OptionType.STRING, "giveaway_title", "The title of the giveaway", false)
                .addOption(OptionType.STRING, "giveaway_message_id", "The message ID of the giveaway", false);
    }

    private static SubcommandData createPlanSubcommand() {
        return new SubcommandData("plan", "Schedule a new giveaway")
                .addOption(OptionType.STRING, "start_time", "The start time of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                .addOption(OptionType.STRING, "title", "The title of the giveaway", true)
                .addOption(OptionType.STRING, "prize", "The prize of the giveaway", true)
                .addOption(OptionType.STRING, "duration", "The duration of the giveaway (e.g., 1d, 1h30m, 2d3h, etc.)", true)
                .addOption(OptionType.INTEGER, "winners", "The number of winners", true)
                .addOption(OptionType.CHANNEL, "channel", "The channel where the giveaway will be announced", false);
    }

    private static SubcommandData createSetLanguageSubcommand() {
        return new SubcommandData("language", "Set the language preference for the bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "language", "The language preference for the bot")
                                .addChoice("English", "en")
                                .addChoice("Romanian", "ro")
                );
    }
}
