package utils;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.JDA;
import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class GiveawayUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayUtil.class);

    private static final ConcurrentHashMap<Long, Timer> giveawayTimers = new ConcurrentHashMap<>();

    public static void scheduleGiveawayEnd(final GiveawayEntity giveaway, JDA jda, GiveawayService giveawayService, WinnerService winnerService, long durationMillis) {
        Timer timer = new Timer();
        giveawayTimers.put(giveaway.getMessageId(), timer);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
            }
        }, durationMillis);
    }

    public static void cancelScheduledGiveawayEnd(GiveawayEntity giveaway) {
        Timer timer = giveawayTimers.remove(giveaway.getMessageId());
        if (timer != null) {
            timer.cancel();
            LOGGER.info("Cancelled the scheduled end for giveaway: {}", giveaway.getTitle());
        }
    }

    public static void endGiveaway(final GiveawayEntity giveaway, JDA jda, final long messageId, GiveawayService giveawayService, WinnerService winnerService) {
        // Reload the giveaway from the database to ensure the entries are up-to-date
        final GiveawayEntity updatedGiveaway = giveawayService.getGiveawayByMessageId(giveaway.getMessageId());
        LOGGER.info("Entries: {}", updatedGiveaway.getEntries());

        List<Long> winners = FairRandomizer.selectWinners(updatedGiveaway.getEntries(), updatedGiveaway.getNumberOfWinners());
        LOGGER.info("Selected winners for giveaway {}: {}", updatedGiveaway.getTitle(), winners);

        TextChannel textChannel = jda.getTextChannelById(updatedGiveaway.getChannelId());
        if (textChannel == null) {
            LOGGER.error("Channel not found for giveaway: {}", updatedGiveaway.getMessageId());
            return;
        }

        if (winners.isEmpty()) {
            textChannel.retrieveMessageById(messageId).queue(message -> {
                message.reply("The giveaway " + updatedGiveaway.getTitle() + " has ended, but there were no entries.").queue();
            });
            return;
        }

        StringBuilder winnerMessage = new StringBuilder("The giveaway " + updatedGiveaway.getTitle() + " has ended! Congratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(updatedGiveaway.getTitle(), winnerId));
        }

        textChannel.retrieveMessageById(messageId).queue(message -> {
            message.reply(winnerMessage.toString()).queue();
        });

        LOGGER.info("Giveaway {} has ended!", updatedGiveaway.getTitle());
    }
}
