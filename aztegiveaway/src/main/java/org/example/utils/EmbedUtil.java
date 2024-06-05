package org.example.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmbedUtil {
    public static EmbedBuilder createGiveawayEmbed(String title, String prize, String duration, int winners, Instant endTime, Long guildId, LocalizationUtil localizationUtil) {
        // Define the Bucharest time zone
        ZoneId bucharestZone = ZoneId.of("Europe/Bucharest");

        // Format the end date and time for Bucharest time
        ZonedDateTime bucharestEndTime = ZonedDateTime.ofInstant(endTime, bucharestZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMMM, HH:mm").withZone(bucharestZone);
        String formattedEndDateTime = formatter.format(bucharestEndTime);

        // Get localized messages
        String prizeLabel = localizationUtil.getLocalizedMessage(guildId, "embed_prize");
        String durationLabel = localizationUtil.getLocalizedMessage(guildId, "embed_duration");
        String winnersLabel = localizationUtil.getLocalizedMessage(guildId, "embed_winners");
        String endTimeLabel = localizationUtil.getLocalizedMessage(guildId, "embed_end_time");
        String footerMessage = localizationUtil.getLocalizedMessage(guildId, "embed_footer").replace("{0}", "🎉");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title)
                .setColor(Color.RED)
                .addField(prizeLabel, prize, false)
                .addField(durationLabel, duration, false)
                .addField(winnersLabel, String.valueOf(winners), false)
                .addField(endTimeLabel, formattedEndDateTime, false)
                .setFooter(footerMessage);

        return embedBuilder;
    }
}
