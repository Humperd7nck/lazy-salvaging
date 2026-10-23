package com.lazysalvaging;

public final class ExtractorTest {
    static int checks;

    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks++;
    }

    static GuideEngine.View view() {
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.cargoKnown = true;
        v.cargoSlots = 100;
        v.inventoryVisible = true;
        v.trackExtractor = true;
        v.extractorReady = true;
        return v;
    }

    static void action(GuideEngine g, GuideEngine.View v, GuideEngine.Action a, String label) {
        check(g.decide(v).action == a, label);
    }

    public static void main(String[] args) {
        GuideEngine g = new GuideEngine();
        GuideEngine.View v = view();
        v.salvaging = true;
        v.salvage = 15;
        v.occupied = 23;
        action(g, v, GuideEngine.Action.NONE, "ready extractor waits for full salvaging inventory");
        v.occupied = 28;
        action(
                g,
                v,
                GuideEngine.Action.HARVEST_EXTRACTOR,
                "full inventory harvests before deposit");
        v.extractorBusy = true;
        action(g, v, GuideEngine.Action.NONE, "harvest animation completes before deposit");
        v.extractorReady = v.extractorBusy = false;
        action(g, v, GuideEngine.Action.DEPOSIT, "harvest completes then deposit");
        v.extractorInactive = true;
        action(
                g,
                v,
                GuideEngine.Action.ACTIVATE_EXTRACTOR,
                "inactive extractor activates at boundary");
        v.trackExtractor = false;
        action(g, v, GuideEngine.Action.DEPOSIT, "option disabled retains old flow");
        v.trackExtractor = true;
        v.extractorInactive = false;
        v.extractorReady = true;
        v.cargoSlots = 210;
        v.occupied = 23;
        action(g, v, GuideEngine.Action.NONE, "crew threshold does not interrupt active inventory");
        check(g.mode == GuideEngine.Mode.ACTIVE, "phase waits until inventory finished");
        v.occupied = 28;
        action(
                g,
                v,
                GuideEngine.Action.HARVEST_EXTRACTOR,
                "threshold boundary runs extractor first");
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.PROCESS;
        v.salvaging = false;
        v.salvage = 12;
        v.sorting = true;
        action(g, v, GuideEngine.Action.NONE, "extractor waits through sorting");
        v.salvage = 0;
        v.sorting = false;
        action(g, v, GuideEngine.Action.HARVEST_EXTRACTOR, "sort completion offers extractor");
        v.extractorReady = false;
        v.inventory.add(
                new GuideEngine.Entry(
                        0, 1, 1, "Silk", GuideEngine.Kind.DROP, GuideEngine.Fit.UNKNOWN));
        action(g, v, GuideEngine.Action.DROP_ITEM, "cleanup resumes");
        v.extractorReady = true;
        action(g, v, GuideEngine.Action.DROP_ITEM, "new readiness cannot interrupt cleanup");
        v.inventory.clear();
        action(g, v, GuideEngine.Action.HARVEST_EXTRACTOR, "cleanup completes before harvest");
        v.extractorReady = false;
        v.cargoSlots = 14;
        v.occupied = 8;
        action(g, v, GuideEngine.Action.DEPLOY, "resume phase transition after extractor");
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.RECHECK;
        v.extractorReady = true;
        v.cargoOpen = true;
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "close interface before extractor");
        v.cargoOpen = false;
        action(g, v, GuideEngine.Action.HARVEST_EXTRACTOR, "extractor checkpoint survives closing");
        for (int base : new int[] {250, 256, 257})
            for (int crew = 0; crew <= 2; crew++) {
                CargoCounter c = new CargoCounter();
                c.reset(1000);
                c.observe(100);
                c.xp(1000 + base + 82 * crew, true, true);
                check(
                        c.known() && c.slots() == 100 + crew,
                        "extractor XP plus crew " + base + "/" + crew);
            }
        CargoCounter c = new CargoCounter();
        c.reset(1000);
        c.observe(100);
        c.xp(1256, true);
        check(!c.known(), "256 without harvesting evidence remains unknown");
        c.reset(10870863);
        c.observe(100);
        c.xp(10870945, true, false);
        c.xp(10871201, true, true);
        check(
                c.known() && c.slots() == 101,
                "crew reward followed by extractor reward adds one cargo slot");
        System.out.println("Extractor: " + checks + " checks passed.");
    }
}
