package utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class EmbedUtil {

    public static EmbedBuilder createGiveawayEmbed(String title, String prize, String duration, int winners, Instant endTime) {
        // Define the Bucharest time zone
        ZoneId bucharestZone = ZoneId.of("Europe/Bucharest");

        // Format the end date and time for Bucharest time
        ZonedDateTime bucharestEndTime = ZonedDateTime.ofInstant(endTime, bucharestZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMMM, HH:mm").withZone(bucharestZone);
        String formattedEndDateTime = formatter.format(bucharestEndTime);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title)
                .setColor(Color.RED)
                .addField("Prize", prize, false)
                .addField("Duration", duration, false)
                .addField("Number of winners", String.valueOf(winners), false)
                .addField("Ends On (Bucharest Time - UTC+3)", formattedEndDateTime, false)
                .setFooter("React with 🎉 to enter!");

        return embedBuilder;
    }
}
