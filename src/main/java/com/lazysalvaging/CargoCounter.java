package com.lazysalvaging;

import java.util.*;

/** Session-only occupied-slot estimate; an open-hold observation always wins. */
final class CargoCounter {
    private int slots = -1, lastXp = -1;
    private boolean known;
    private String reason = "Open cargo hold to initialize counter";

    void reset(int xp) {
        slots = -1;
        known = false;
        lastXp = xp;
        reason = "Open cargo hold to initialize counter";
    }

    boolean known() {
        return known;
    }

    int slots() {
        return slots;
    }

    String reason() {
        return reason;
    }

    void baselineIfMissing(int xp) {
        if (lastXp < 0) lastXp = xp;
    }

    void uncertain(String why) {
        known = false;
        reason = why;
    }

    void observe(int occupied) {
        if (occupied < 0 || occupied > 240) {
            uncertain("Invalid cargo reading");
            return;
        }
        slots = occupied;
        known = true;
        reason = "";
    }

    // Tolerance applies to the observed total, not separately to each component.
    static final int XP_TOLERANCE = 1;
    static final int[][] NORMAL_XP = {{97, 98, 205}, {82, 179, 180, 287}, {164, 261, 262, 369}};

    static int crewForXp(int delta) {
        return matchXp(delta, NORMAL_XP);
    }

    private static int matchXp(int delta, int[][] values) {
        int matched = -1;
        for (int crew = 0; crew < values.length; crew++)
            for (int value : values[crew])
                if (Math.abs((long) delta - value) <= XP_TOLERANCE) {
                    if (matched >= 0 && matched != crew) return -1;
                    matched = crew;
                }
        return matched;
    }

    void xp(int total, boolean inSession) {
        xp(total, inSession, false);
    }

    static int extractorCrewForXp(int delta) {
        int[][] values = new int[3][3];
        for (int crew = 0; crew <= 2; crew++) {
            values[crew][0] = 250 + 82 * crew;
            values[crew][1] = 256 + 82 * crew;
            values[crew][2] = 257 + 82 * crew;
        }
        return matchXp(delta, values);
    }

    void xp(int total, boolean inSession, boolean harvestingExtractor) {
        int previous = lastXp;
        lastXp = total;
        if (previous < 0 || total == previous) return;
        int delta = total - previous, crew = crewForXp(delta);
        if (harvestingExtractor) {
            int extractorCrew = extractorCrewForXp(delta);
            if (extractorCrew >= 0) crew = crew >= 0 && crew != extractorCrew ? -1 : extractorCrew;
        }
        if (!known) return;
        if (!inSession || crew < 0) {
            uncertain("Unrecognized Sailing XP change: " + delta);
            return;
        }
        adjust(crew);
    }

    void transferSalvage(int depositedMinusWithdrawn) {
        if (known) adjust(depositedMinusWithdrawn);
    }

    void transfer(
            Map<Integer, Integer> before,
            Map<Integer, Integer> after,
            int direction,
            boolean sawSnapshot) {
        Set<Integer> ids = new HashSet<>(after.keySet());
        ids.addAll(before.keySet());
        int salvageDelta = 0;
        boolean other = false;
        for (int id : ids) {
            int delta = before.getOrDefault(id, 0) - after.getOrDefault(id, 0);
            if (id == 32861) salvageDelta = delta;
            else if (delta != 0) other = true;
        }
        if (sawSnapshot || other || salvageDelta * direction <= 0)
            uncertain("Ambiguous cargo transfer; reopen hold");
        else transferSalvage(salvageDelta);
    }

    private void adjust(int delta) {
        long next = (long) slots + delta;
        if (next < 0 || next > 240) {
            uncertain("Cargo estimate outside capacity; reopen hold");
            return;
        }
        slots = (int) next;
    }
}
