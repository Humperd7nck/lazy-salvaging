package com.lazysalvaging;

/** A matching harvest reward ends the wait even if the animation is still finishing. */
final class ExtractorHarvest {
    private boolean pending, confirmed;
    private int lastXp, deadline;

    void cancelPending() {
        pending = false;
    }

    void reset() {
        pending = confirmed = false;
    }

    void start(int tick, int xp) {
        pending = true;
        confirmed = false;
        lastXp = xp;
        deadline = tick + 6;
    }

    boolean xp(int total, int tick) {
        if (!pending) return false;
        int delta = total - lastXp;
        lastXp = total;
        if (tick > deadline) {
            pending = false;
            return false;
        }
        if (CargoCounter.extractorCrewForXp(delta) < 0) return false;
        pending = false;
        confirmed = true;
        return true;
    }

    int deadline() {
        return deadline;
    }

    boolean confirmed() {
        return confirmed;
    }

    void observe(boolean ready, boolean harvestAnimation, int tick) {
        if (tick > deadline) pending = false;
        if (confirmed && !ready && !harvestAnimation) confirmed = false;
    }
}
