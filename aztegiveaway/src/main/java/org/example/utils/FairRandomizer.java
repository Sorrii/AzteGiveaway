/**
 * This class is used to randomly select winners from a list of entries.
 * It uses a secure random number generator to ensure fairness.
 */

package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

public class FairRandomizer {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Logger LOGGER = LoggerFactory.getLogger(FairRandomizer.class);

    /**
     * Shuffles the list of entries and selects a specified number of winners
     *
     * @param entries         The list of entries (participants) to select winners from
     * @param numberOfWinners The number of winners to select
     * @param <T>             The type of entries in the list
     * @return A list of selected winners
     */
    public static <T> List<T> selectWinners(List<T> entries, int numberOfWinners) {
        if (entries == null || entries.isEmpty() || numberOfWinners <= 0) {
            LOGGER.warn("Invalid input for selecting winners. Entries: {}, Number of winners: {}", entries, numberOfWinners);
            return Collections.emptyList();
        }

        Collections.shuffle(entries, SECURE_RANDOM);
        return entries.subList(0, Math.min(numberOfWinners, entries.size()));
    }
}
