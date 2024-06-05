package org.example.utils;

import commands.GiveawayCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmbedUtil {
    public static EmbedBuilder createGiveawayEmbed(String title, String prize, String duration, int winners, Instant endTime, Long guildId, LocalizationUtil localizationUtil) {
        // Define the Bucharest time zone (as main server is in Bucharest)
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

    public static List<EmbedBuilder> createPaginatedEmbeds(List<String> messages, int itemsPerPage) {
        List<EmbedBuilder> embeds = new ArrayList<>();
        int totalPages = (int) Math.ceil((double) messages.size() / itemsPerPage);

        for (int i = 0; i < totalPages; i++) {
            int start = i * itemsPerPage;
            int end = Math.min(start + itemsPerPage, messages.size());

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder
                    .setTitle(" (Page " + (i + 1) + "/" + totalPages + ")")
                    .setColor(Color.RED);

            StringBuilder pageContent = new StringBuilder();
            for (int j = start; j < end; j++) {
                pageContent.append(messages.get(j)).append("\n");
            }

            embedBuilder.setDescription(pageContent.toString());
            embeds.add(embedBuilder);
        }

        return embeds;
    }
}
