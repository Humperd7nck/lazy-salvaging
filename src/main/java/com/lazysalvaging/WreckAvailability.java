package com.lazysalvaging;

final class WreckAvailability {
    // RuneLite gameval ObjectID1; Sailing's salvaging overlay uses a 15x15 area.
    static final int ACTIVE_MERCHANT_WRECK = 60478;

    static boolean reachable(int resolvedId, int hookX, int hookY, int wreckX, int wreckY) {
        return resolvedId == ACTIVE_MERCHANT_WRECK
                && Math.abs(hookX - wreckX) <= 7
                && Math.abs(hookY - wreckY) <= 7;
    }
}
