/**
 * This class provides utility methods for parsing durations from strings
 */

package utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DurationParser {

    /**
     * Parses a duration string and returns a Duration object
     *
     * @param durationStr The duration string (e.g., "1h", "30m", "2d")
     * @return A Duration object representing the parsed duration
     * @throws IllegalArgumentException if the duration string is invalid
     */
    public static Duration parseDuration(String durationStr) {
        if (durationStr.endsWith("h")) {
            long hours = Long.parseLong(durationStr.replace("h", ""));
            return Duration.of(hours, ChronoUnit.HOURS);
        } else if (durationStr.endsWith("m")) {
            long minutes = Long.parseLong(durationStr.replace("m", ""));
            return Duration.of(minutes, ChronoUnit.MINUTES);
        } else if (durationStr.endsWith("d")) {
            long days = Long.parseLong(durationStr.replace("d", ""));
            return Duration.of(days, ChronoUnit.DAYS);
        } else {
            throw new IllegalArgumentException("Invalid duration format");
        }
    }
}
