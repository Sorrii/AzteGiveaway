/**
 * This class is used to randomly select winners from a list of entries.
 * It uses a secure random number generator to ensure fairness.
 */

package org.example.utils;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

public class FairRandomizer {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Shuffles the list of entries and selects a specified number of winners
     *
     * @param entries         The list of entries (participants) to select winners from
     * @param numberOfWinners The number of winners to select
     * @param <T>             The type of entries in the list
     * @return A list of selected winners
     */
    public static <T> List<T> selectWinners(List<T> entries, int numberOfWinners) {
        Collections.shuffle(entries, SECURE_RANDOM);
        return entries.subList(0, Math.min(numberOfWinners, entries.size()));
    }
}
