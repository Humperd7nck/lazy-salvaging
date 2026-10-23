package com.lazysalvaging;

public final class CargoCounterTest {
    static int checks;

    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }

    public static void main(String[] args) {
        int[] gains = {82, 164, 97, 98, 179, 180, 261, 262, 205, 287, 369};
        int[] crew = {1, 2, 0, 0, 1, 1, 2, 2, 0, 1, 2};
        for (int i = 0; i < gains.length; i++) {
            CargoCounter c = new CargoCounter();
            c.reset(1000);
            c.observe(100);
            c.xp(1000 + gains[i], true);
            check(c.known() && c.slots() == 100 + crew[i], "XP " + gains[i]);
            c.xp(1000 + gains[i], true);
            check(c.slots() == 100 + crew[i], "duplicate total ignored " + gains[i]);
        }
        CargoCounter c = new CargoCounter();
        c.reset(1000);
        c.observe(100);
        c.xp(1097, true);
        c.xp(1179, true);
        c.xp(1261, true);
        check(c.slots() == 102, "separate sort and crew events counted once each");
        c.transferSalvage(20);
        check(c.slots() == 122, "confirmed deposit");
        c.transferSalvage(-20);
        check(c.slots() == 102, "confirmed withdrawal");
        c.observe(102);
        check(c.slots() == 102, "snapshot replaces rather than adds XP or transfer");
        c.observe(75);
        check(c.slots() == 75, "snapshot corrects discrepancy");
        c.xp(1262, true);
        check(!c.known(), "unknown XP invalidates");
        c.xp(1344, true);
        check(!c.known(), "recognized XP cannot restore uncertain count");
        c.observe(20);
        c.xp(1426, false);
        check(!c.known(), "XP outside session invalidates");
        c.observe(240);
        c.xp(1508, true);
        check(!c.known(), "overflow is not silently clamped");
        c.observe(5);
        c.transferSalvage(-6);
        check(!c.known(), "underflow invalidates");
        c.reset(-1);
        c.xp(9000, true);
        check(!c.known(), "login XP initializes baseline only");
        c.observe(10);
        c.xp(9082, true);
        check(c.slots() == 11, "first gain after baseline counted");
        c.xp(8000, true);
        check(!c.known(), "XP rollback invalidates");
        c.reset(8000);
        check(!c.known(), "reset needs opening");

        java.util.Map<Integer, Integer> before = java.util.Map.of(32861, 20, 2, 100);
        java.util.Map<Integer, Integer> after = java.util.Map.of(2, 100);
        c.observe(100);
        c.transfer(before, after, 1, false);
        check(c.slots() == 120, "inventory-confirmed deposit");
        c.transfer(after, before, -1, false);
        check(c.slots() == 100, "inventory-confirmed withdrawal");
        c.xp(8082, true);
        c.transfer(before, after, 1, false);
        check(c.slots() == 121, "crew XP plus deposit");
        c.observe(121);
        c.transfer(before, after, 1, true);
        check(!c.known(), "snapshot overlap requires reread instead of double count");
        c.observe(121);
        check(c.known() && c.slots() == 121, "open snapshot restores authoritative total");
        c.transfer(java.util.Map.of(2, 100), java.util.Map.of(2, 50), 1, false);
        check(!c.known(), "stack quantity changes never become fifty slots");
        c.observe(10);
        c.transfer(after, before, 1, false);
        check(!c.known(), "wrong transfer direction invalidates");

        GuideEngine g = new GuideEngine();
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.cargoKnown = true;
        v.cargoSlots = 209;
        v.occupied = 8;
        check(g.decide(v).action == GuideEngine.Action.DEPLOY, "known closed cargo starts active");
        v.cargoSlots = 210;
        check(
                g.decide(v).action == GuideEngine.Action.OPEN_CARGO
                        && g.mode == GuideEngine.Mode.SORTING,
                "crew XP reaches threshold while closed");
        g.stage = GuideEngine.Stage.CLEANUP;
        g.assignRequired = false;
        v.cargoSlots = 14;
        v.inventoryVisible = true;
        check(
                g.decide(v).action == GuideEngine.Action.DEPLOY
                        && g.mode == GuideEngine.Mode.ACTIVE,
                "14 closed slots returns active without reopening");
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.CLEANUP;
        v.cargoSlots = 15;
        check(
                g.decide(v).action == GuideEngine.Action.OPEN_CARGO
                        && g.mode == GuideEngine.Mode.SORTING,
                "15 opens for withdrawal");
        g.stage = GuideEngine.Stage.CLEANUP;
        v.cargoKnown = false;
        check(
                g.decide(v).action == GuideEngine.Action.OPEN_CARGO
                        && g.mode == GuideEngine.Mode.SORTING,
                "uncertainty keeps sorting phase while refreshing");
        System.out.println("Cargo counter: " + checks + " checks passed.");
    }
}
