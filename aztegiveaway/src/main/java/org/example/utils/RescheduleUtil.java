package org.example.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import org.example.entities.GiveawayEntity;
import org.example.services.GiveawayService;
import org.example.services.WinnerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RescheduleUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RescheduleUtil.class);
    private final JDA jda;
    private final GiveawayService giveawayService;
    private final WinnerService winnerService;
    private final LocalizationUtil localizationUtil;

    public RescheduleUtil(JDA jda, GiveawayService giveawayService, WinnerService winnerService, LocalizationUtil localizationUtil) {
        this.jda = jda;
        this.giveawayService = giveawayService;
        this.winnerService = winnerService;
        this.localizationUtil = localizationUtil;
    }

    public void rescheduleActiveAndPlannedGiveaways() {
        List<GiveawayEntity> activeGiveaways = giveawayService.getAllGiveaways();
        GiveawayUtil giveawayUtil = new GiveawayUtil(localizationUtil);

        for (GiveawayEntity giveaway : activeGiveaways) {
            long currentTime = Instant.now().toEpochMilli();
            long startTime = giveaway.getStartTime().toEpochMilli();
            long remainingTime = giveaway.getDuration() - (currentTime - startTime);

            if (startTime > currentTime) {
                scheduleFutureGiveaway(giveaway, startTime, currentTime, giveawayUtil);
            } else if (remainingTime > 0) {
                giveawayUtil.scheduleGiveawayEnd(giveaway, jda, giveawayService, winnerService, remainingTime);
                LOGGER.info("Rescheduled giveaway {} with remaining time {} ms", giveaway.getTitle(), remainingTime);
            } else {
                giveawayUtil.endGiveaway(giveaway, jda, giveaway.getMessageId(), giveawayService, winnerService);
            }
        }
    }

    private void scheduleFutureGiveaway(GiveawayEntity giveaway, long startTime, long currentTime, GiveawayUtil giveawayUtil) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
                if (textChannel != null) {
                    EmbedBuilder embedBuilder = EmbedUtil.createGiveawayEmbed(
                            giveaway.getTitle(),
                            giveaway.getPrize(),
                            giveaway.getDuration() / 1000 + "s",
                            giveaway.getNumberOfWinners(),
                            Instant.now().plusMillis(giveaway.getDuration()),
                            giveaway.getGuildId(),
                            localizationUtil
                    );

                    textChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                        message.addReaction(Emoji.fromUnicode("🎉")).queue();
                        giveaway.setMessageId(message.getIdLong());
                        giveawayService.updateGiveaway(giveaway);
                        giveawayUtil.scheduleGiveawayEnd(giveaway, jda, giveawayService, winnerService, giveaway.getDuration());
                    });
                } else {
                    LOGGER.warn("Text channel not found for giveaway {}: {}", giveaway.getTitle(), giveaway.getChannelId());
                }
            }
        }, startTime - currentTime);

        LOGGER.info("Scheduled future giveaway {} to start at {}", giveaway.getTitle(), giveaway.getStartTime());
    }
}
