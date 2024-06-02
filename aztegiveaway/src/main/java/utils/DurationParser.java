package utils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    /**
     * Parses a complex duration string and returns the total duration in milliseconds
     *
     * @param durationStr The duration string (e.g., "1h30m", "1h20s", "xdymzs")
     * @return The total duration in milliseconds
     * @throws IllegalArgumentException if the duration string is invalid
     */
    public static long parseDuration(String durationStr) {
        // Regular expression to match patterns like "1d", "2h", "30m", "20s"
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(durationStr);

        long totalMillis = 0;

        // Iterating over the matches and adding the corresponding duration to the total
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d" -> totalMillis += Duration.ofDays(value).toMillis();
                case "h" -> totalMillis += Duration.ofHours(value).toMillis();
                case "m" -> totalMillis += Duration.ofMinutes(value).toMillis();
                case "s" -> totalMillis += Duration.ofSeconds(value).toMillis();
                default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
            }
        }

        if (totalMillis == 0) {
            throw new IllegalArgumentException("Invalid duration format");
        }

        return totalMillis;
    }
}
