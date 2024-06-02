package utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GiveawayUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayUtil.class);

    public static void endGiveaway(final GiveawayEntity giveaway, JDA jda, final long messageId, GiveawayService giveawayService, WinnerService winnerService) {
        // Reload the giveaway from the database to ensure the entries are up-to-date
        // updatedGiveaway is final because it is used in the lambda expression below
        final GiveawayEntity updatedGiveaway = giveawayService.getGiveawayByMessageId(giveaway.getMessageId());
        LOGGER.info("Entries: {}", updatedGiveaway.getEntries());

        List<Long> winners = FairRandomizer.selectWinners(updatedGiveaway.getEntries(), updatedGiveaway.getNumberOfWinners());
        LOGGER.info("Selected winners for giveaway {}: {}", updatedGiveaway.getEntries(), winners);

        TextChannel textChannel = jda.getTextChannelById(updatedGiveaway.getChannelId());
        if (textChannel == null) {
            LOGGER.error("Channel not found for giveaway: {}", updatedGiveaway.getMessageId());
            return;
        }

        if (winners.isEmpty()) {
            textChannel.retrieveMessageById(messageId).queue(message -> {
                message.reply("The giveaway " + updatedGiveaway.getTitle() + " has ended, but there were no entries.").queue();
                giveawayService.deleteGiveaway(updatedGiveaway.getId());
            });
            return;
        }

        StringBuilder winnerMessage = new StringBuilder("The giveaway " + updatedGiveaway.getTitle() + " has ended!\nCongratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(updatedGiveaway.getTitle(), winnerId));
        }

        textChannel.retrieveMessageById(messageId).queue(message -> {
            message.reply(winnerMessage.toString()).queue();
            giveawayService.deleteGiveaway(updatedGiveaway.getId());
        });

        LOGGER.info("Giveaway {} has ended!", updatedGiveaway.getTitle());
    }
}
