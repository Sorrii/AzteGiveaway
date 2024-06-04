package utils;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.JDA;

import org.example.entities.GiveawayEntity;
import org.example.entities.WinnerEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GiveawayUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayUtil.class);

    // Map to store the timers for each giveaway
    private static final Map<Long, Timer> giveawayTimers = new ConcurrentHashMap<>();

    // Map to store reminder timers for each giveaway
    private static final Map<Long, List<Timer>> reminderTimers = new ConcurrentHashMap<>();
    private static final int REMINDER_PERCENTAGE_50 = 50;
    private static final int REMINDER_PERCENTAGE_90 = 90;

    public static void scheduleGiveawayEnd(final GiveawayEntity giveaway, JDA jda, GiveawayService giveawayService, WinnerService winnerService, long durationMillis) {
        Timer timer = new Timer();
        giveawayTimers.put(giveaway.getMessageId(), timer);

        scheduleReminders(giveaway, jda, durationMillis); // Schedule reminders for the giveaway

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
            }
        }, durationMillis);
    }

    public static boolean cancelScheduledGiveawayEnd(GiveawayEntity giveaway) {
        Timer timer = giveawayTimers.remove(giveaway.getMessageId());
        if (timer != null) {
            timer.cancel();
            cancelReminderTimers(giveaway.getMessageId());
            LOGGER.info("Cancelled the scheduled end for giveaway: {}", giveaway.getTitle());
            return true;
        }

        LOGGER.info("No scheduled end found for giveaway: {}", giveaway.getTitle());
        return false;
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
            textChannel.retrieveMessageById(messageId).queue(message -> message.reply("The giveaway " + updatedGiveaway.getTitle() + " has ended, but there were no entries.").queue());
            return;
        }

        StringBuilder winnerMessage = new StringBuilder("The giveaway " + updatedGiveaway.getTitle() + " has ended! Congratulations to the winners:\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(updatedGiveaway.getTitle(), updatedGiveaway.getMessageId(), winnerId));
        }

        giveawayTimers.remove(messageId);
        textChannel.retrieveMessageById(messageId).queue(message -> message.reply(winnerMessage.toString()).queue());

        LOGGER.info("Giveaway {} has ended!", updatedGiveaway.getTitle());
    }

    // Method used to schedule reminders for the giveaway
    private static void scheduleReminders(GiveawayEntity giveaway, JDA jda, long durationMillis) {
        Instant startTime = giveaway.getStartTime();

        long reminderTime50Percent = startTime.plusMillis(durationMillis / 2).toEpochMilli();
        long reminderTime90Percent = startTime.plusMillis((long) (durationMillis * 0.9)).toEpochMilli();

        scheduleReminder(giveaway, jda, reminderTime50Percent, REMINDER_PERCENTAGE_50);
        scheduleReminder(giveaway, jda, reminderTime90Percent, REMINDER_PERCENTAGE_90);
    }

    private static void scheduleReminder(GiveawayEntity giveaway, JDA jda, long reminderTime, int percentage) {
        long currentTime = Instant.now().toEpochMilli();
        if (reminderTime > currentTime) {
            Timer reminderTimer = new Timer();
            reminderTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendReminder(giveaway, jda, percentage);
                }
            }, reminderTime - currentTime);

            // Add reminder timer to the list of reminders for this giveaway
            reminderTimers.computeIfAbsent(giveaway.getMessageId(), timer -> new ArrayList<>()).add(reminderTimer);
        }
    }

    // Method used to send a reminder message for the giveaway
    private static void sendReminder(GiveawayEntity giveaway, JDA jda, int percentage) {
        TextChannel channel = jda.getTextChannelById(giveaway.getChannelId());
        if (channel != null) {
            String message;
            if (percentage == 50) {
                String winnerText = giveaway.getNumberOfWinners() == 1 ? "winner" : "winners";
                message = "Half way to announcing the " + winnerText + "! Make sure to enter! @everyone";
            } else {
                message = "Giveaway has almost ended! Make sure you joined it! @everyone";
            }
            channel.retrieveMessageById(giveaway.getMessageId()) // Fetch the giveaway message
                    .queue(giveawayMessage -> giveawayMessage.reply(message).queue(),  // Reply to it if found
                            error -> LOGGER.warn("Giveaway message not found for ID: {}", giveaway.getMessageId())); // Handle if not found
        } else {
            LOGGER.warn("Channel not found for giveaway: {}", giveaway.getTitle());
        }
    }

    // Cancel all reminder timers for a giveaway
    private static void cancelReminderTimers(Long messageId) {
        List<Timer> timers = reminderTimers.remove(messageId);
        if (timers != null) {
            timers.forEach(Timer::cancel);
            LOGGER.info("Cancelled all reminder timers for giveaway with message ID: {}", messageId);
        }
    }
}
