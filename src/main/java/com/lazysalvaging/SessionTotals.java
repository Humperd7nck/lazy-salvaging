package com.lazysalvaging;

/** Confirmed Sailing rewards, never menu clicks. Totals survive a cargo-guide reset. */
final class SessionTotals {
    private int lastXp = -1;
    private long sorted, harvests;

    long sorted() {
        return sorted;
    }

    long harvests() {
        return harvests;
    }

    void baseline(int xp) {
        lastXp = xp;
    }

    void baselineIfMissing(int xp) {
        if (lastXp < 0) baseline(xp);
    }

    void reset(int xp) {
        sorted = harvests = 0;
        baseline(xp);
    }

    void xp(int total, boolean here, boolean sorting, boolean harvesting) {
        int previous = lastXp;
        lastXp = total;
        if (!here || previous < 0 || total <= previous) return;
        int delta = total - previous;
        if (harvesting && CargoCounter.extractorCrewForXp(delta) >= 0) {
            harvests++;
            return;
        }
        if (sorting)
            for (int crew = 0; crew <= 2; crew++)
                if (delta >= 96 + 82 * crew && delta <= 99 + 82 * crew) {
                    sorted++;
                    return;
                }
    }
}
