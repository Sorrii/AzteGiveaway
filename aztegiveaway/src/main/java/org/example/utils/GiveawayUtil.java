/**
 * Utility class to handle scheduling and ending giveaways.
 * It uses a Timer to schedule the end of a giveaway and the FairRandomizer to select the winners.
 * It also schedules reminders for the giveaway at 50% and 90% of the duration.
 */

package org.example.utils;

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

    private static LocalizationUtil localizationUtil;
    private static final int REMINDER_PERCENTAGE_50 = 50;
    private static final int REMINDER_PERCENTAGE_90 = 90;

    public GiveawayUtil(LocalizationUtil localizationUtil) {
        GiveawayUtil.localizationUtil = localizationUtil;
    }

    public void scheduleGiveawayEnd(final GiveawayEntity giveaway, JDA jda, GiveawayService giveawayService, WinnerService winnerService, long durationMillis) {
        Timer timer = new Timer();
        giveawayTimers.put(giveaway.getMessageId(), timer);

        scheduleReminders(giveaway, jda, durationMillis); // Schedule reminders for the giveaway

        // Schedule the end of the giveaway after <durationMillis> milliseconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
            }
        }, durationMillis);
    }

    // Method is boolean to indicate if the giveaway was found and cancelled
    public static boolean cancelScheduledGiveawayEnd(GiveawayEntity giveaway) {
        // Cancel the scheduled end for the giveaway and remove it from the map
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

    public void endGiveaway(final GiveawayEntity giveaway, JDA jda, final long messageId, GiveawayService giveawayService, WinnerService winnerService) {
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

        Long guildId = giveaway.getGuildId();

        if (winners.isEmpty()) {
            String noEntriesMessage = localizationUtil.getLocalizedMessage(guildId, "giveaway_no_entries").replace("{0}", updatedGiveaway.getTitle());
            textChannel.retrieveMessageById(messageId).queue(message -> message.reply(noEntriesMessage).queue());
            return;
        }

        // small gimmick to show the winner/winners based on the number of winners
        String winnerText = giveaway.getNumberOfWinners() == 1 ? localizationUtil.getLocalizedMessage(guildId, "winner") : localizationUtil.getLocalizedMessage(guildId, "winners");
        StringBuilder winnerMessage = new StringBuilder(localizationUtil.getLocalizedMessage(guildId, "giveaway_winner_message").replace("{0}", updatedGiveaway.getTitle()) + " " + winnerText + ":\n");
        for (Long winnerId : winners) {
            winnerMessage.append("<@").append(winnerId).append(">\n");
            winnerService.addWinner(new WinnerEntity(updatedGiveaway.getTitle(), updatedGiveaway.getMessageId(), winnerId, guildId));
        }

        giveawayTimers.remove(messageId);
        textChannel.retrieveMessageById(messageId).queue(message -> message.reply(winnerMessage.toString()).queue());

        LOGGER.info("Giveaway {} has ended!", updatedGiveaway.getTitle());
    }

    // Method used to schedule reminders for the giveaway
    private void scheduleReminders(GiveawayEntity giveaway, JDA jda, long durationMillis) {
        Instant startTime = giveaway.getStartTime();

        long reminderTime50Percent = startTime.plusMillis(durationMillis / 2).toEpochMilli();
        long reminderTime90Percent = startTime.plusMillis((long) (durationMillis * 0.9)).toEpochMilli();

        scheduleReminder(giveaway, jda, reminderTime50Percent, REMINDER_PERCENTAGE_50);
        scheduleReminder(giveaway, jda, reminderTime90Percent, REMINDER_PERCENTAGE_90);
    }

    private void scheduleReminder(GiveawayEntity giveaway, JDA jda, long reminderTime, int percentage) {
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
            Long guildId = giveaway.getGuildId();
            String message;
            if (percentage == 50) {
                String winnerText = giveaway.getNumberOfWinners() == 1 ? localizationUtil.getLocalizedMessage(guildId, "winner") : localizationUtil.getLocalizedMessage(guildId, "winners");
                message = localizationUtil.getLocalizedMessage(guildId, "giveaway_halfway_reminder").replace("{0}", winnerText);
            } else {
                message = localizationUtil.getLocalizedMessage(guildId, "giveaway_almost_ended_reminder");
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