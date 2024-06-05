package org.example.utils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    // Regular expression to match a duration string like "1d2h30m15s"
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");

    public static long parseDuration(String durationStr) {
        // Double-checking never hurts
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return 0;
        }

        Matcher matcher = DURATION_PATTERN.matcher(durationStr.trim());

        long totalMillis = 0;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d" -> totalMillis += Duration.ofDays(value).toMillis();
                case "h" -> totalMillis += Duration.ofHours(value).toMillis();
                case "m" -> totalMillis += Duration.ofMinutes(value).toMillis();
                case "s" -> totalMillis += Duration.ofSeconds(value).toMillis();
                default -> totalMillis += 0; // Ignore unknown units
            }
        }

        return totalMillis;
    }
}
