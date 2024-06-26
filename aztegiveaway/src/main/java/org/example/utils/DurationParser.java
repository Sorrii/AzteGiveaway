/**
 * Utility class to parse a duration string like "1d2h30m15s" into milliseconds.
 * It returns 0 if the input string is null or empty or the format is not respected.
 * It transforms the duration string into milliseconds by parsing the days, hours, minutes, and seconds.
 */

package org.example.utils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    // Regular expression to match a duration string like "1d2h30m15s"
    // \d+ matches one or more digits (at least one digit needs to be parsed)
    // [dhms] matches one of the characters 'd', 'h', 'm', 's'
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");

    public static long parseDuration(String durationStr) {
        // Double-checking never hurts
        if (durationStr == null || durationStr.isBlank()) {
            return 0;
        }

        Matcher matcher = DURATION_PATTERN.matcher(durationStr.trim());

        long totalMillis = 0;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            totalMillis += switch (matcher.group(2)) {
                case "d" -> Duration.ofDays(value).toMillis();
                case "h" -> Duration.ofHours(value).toMillis();
                case "m" -> Duration.ofMinutes(value).toMillis();
                case "s" -> Duration.ofSeconds(value).toMillis();
                default -> 0;
            };
        }

        return totalMillis;
    }
}
