package com.lazysalvaging;

/** Tolerate one idle animation tick, but never latch activity on an operating varbit. */
final class SalvagingActivity {
    private int idleTicks = 2;

    void reset() {
        idleTicks = 2;
    }

    boolean observe(int animation) {
        if (animation == 13583 || animation == 13584) idleTicks = 0;
        else idleTicks = Math.min(2, idleTicks + 1);
        return idleTicks < 2;
    }
}
