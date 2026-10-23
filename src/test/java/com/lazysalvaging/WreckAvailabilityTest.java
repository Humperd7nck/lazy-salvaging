package com.lazysalvaging;

public final class WreckAvailabilityTest {
    private static int checks;

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }

    public static void main(String[] args) {
        check(WreckAvailability.reachable(60478, 50, 50, 57, 57), "range boundary");
        check(!WreckAvailability.reachable(60478, 50, 50, 58, 50), "active but out of range");
        check(!WreckAvailability.reachable(60479, 50, 50, 50, 50), "depleted stump excluded");
        check(!WreckAvailability.reachable(60510, 50, 50, 50, 50), "hook is not a wreck");
        GuideEngine g = new GuideEngine();
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.cargoKnown = true;
        v.cargoSlots = 100;
        v.depleted = true;
        check(g.decide(v).action == GuideEngine.Action.NONE, "both wrecks down waits");
        check(g.decide(v).preview == GuideEngine.Action.NONE, "no hook preview while waiting");
        v.wreckAvailable = true;
        check(
                g.decide(v).action == GuideEngine.Action.DEPLOY,
                "either wreck respawning restores deploy");
        v.salvaging = true;
        check(
                g.decide(v).action == GuideEngine.Action.DEPLOY,
                "replacement wreck still overrides stale animation");
        v.depleted = false;
        check(g.decide(v).action == GuideEngine.Action.NONE, "active salvaging continues");
        v.wreckAvailable = false;
        v.occupied = 28;
        v.salvage = 20;
        check(
                g.decide(v).action == GuideEngine.Action.DEPOSIT,
                "full inventory still deposits with no wreck");
        v.occupied = 5;
        v.salvage = 0;
        v.cargoOpen = true;
        check(
                g.decide(v).action == GuideEngine.Action.CLOSE_CARGO
                        && g.decide(v).preview == GuideEngine.Action.NONE,
                "no stale close-cargo hook preview");
        v.cargoOpen = false;
        v.trackExtractor = true;
        v.extractorReady = true;
        check(
                g.decide(v).action == GuideEngine.Action.HARVEST_EXTRACTOR,
                "extractor can be tended while wrecks down");
        v.trackExtractor = false;
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.PROCESS;
        v.salvage = 10;
        v.salvaging = false;
        check(g.decide(v).action == GuideEngine.Action.SORT, "sorting unaffected");
        System.out.println("Wreck availability: " + checks + " checks passed.");
    }
}
