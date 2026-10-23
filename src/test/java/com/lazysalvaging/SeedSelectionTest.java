package com.lazysalvaging;

import java.util.*;

public final class SeedSelectionTest {
    private static int checks;

    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    public static void main(String[] args) throws Exception {
        Set<SalvageSeed> selected = EnumSet.of(SalvageSeed.BELLADONNA, SalvageSeed.IRIT);
        expect(
                SalvageSeed.classify("Belladonna seed", selected) == GuideEngine.Kind.SELECTED_SEED,
                "selection overrides drop list");
        expect(
                SalvageSeed.classify("Cactus seed", selected) == GuideEngine.Kind.DROP,
                "unselected explicit drop");
        expect(
                SalvageSeed.classify("Avantoe seed", selected) == GuideEngine.Kind.OTHER,
                "unselected former bag-dependent seed retained");
        expect(
                SalvageSeed.classify("Torstol seed", selected) == GuideEngine.Kind.OTHER,
                "unselected valuable seed retained");
        expect(
                !DropMenuSwap.eligible(GuideEngine.Kind.SELECTED_SEED, GuideEngine.Fit.NO),
                "selected seed never overflow dropped");
        GuideEngine g = new GuideEngine();
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.CLEANUP;
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.inventoryVisible = true;
        v.seedsPresent = true;
        v.cargoKnown = true;
        v.cargoSlots = 10;
        v.inventory.add(
                new GuideEngine.Entry(
                        3,
                        1,
                        10,
                        "Irit seed",
                        GuideEngine.Kind.SELECTED_SEED,
                        GuideEngine.Fit.UNKNOWN));
        expect(
                g.decide(v).action == GuideEngine.Action.SELECT_SEED,
                "unknown contents need no Check");
        expect(g.decide(v).preview == GuideEngine.Action.USE_SEED_BOX, "box preview");
        v.selectedSeedSlot = 3;
        expect(g.decide(v).action == GuideEngine.Action.USE_SEED_BOX, "selected seed targets box");
        v.selectedSeedSlot = -1;
        expect(
                g.decide(v).action == GuideEngine.Action.SELECT_SEED,
                "cancel selection returns to seed");
        v.seedDepositBlocked = true;
        expect(
                g.decide(v).action == GuideEngine.Action.CHECK_SEEDS,
                "unconfirmed deposit requests authoritative contents");
        v.seedDepositBlocked = false;
        v.seedsPresent = false;
        expect(
                g.decide(v).action == GuideEngine.Action.DEPLOY,
                "missing box keeps seeds and continues");
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.CLEANUP;
        v.seedsPresent = true;
        v.seedSelectionInvalid = true;
        expect(
                g.decide(v).action == GuideEngine.Action.NONE,
                "more than six native selections pauses");
        v.seedSelectionInvalid = false;
        v.inventory.clear();
        expect(g.decide(v).action == GuideEngine.Action.DEPLOY, "resume when selected seeds gone");
        System.out.println("Seed selection: " + checks + " checks passed.");
    }
}
