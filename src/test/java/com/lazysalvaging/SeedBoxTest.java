package com.lazysalvaging;

import java.util.*;

public final class SeedBoxTest {
    public static void main(String[] args) {
        BagKnowledge bags = new BagKnowledge();
        GuideEngine engine = new GuideEngine();
        engine.mode = GuideEngine.Mode.SORTING;
        engine.stage = GuideEngine.Stage.CLEANUP;
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.seedsPresent = true;
        v.seedBoxOpen = true;
        expect(engine.decide(v).action == GuideEngine.Action.NONE, "wait for contents");
        Map<String, Integer> seeds = new HashMap<>();
        for (String name :
                new String[] {
                    "irit seed",
                    "ranarr seed",
                    "avantoe seed",
                    "kwuarm seed",
                    "snapdragon seed",
                    "torstol seed"
                }) seeds.put(name, 3);
        bags.request(GuideEngine.Kind.SEED, 1);
        bags.tick(
                20); // Even a delayed/repeated interface opening must not rely on chat or a new
                     // event.
        bags.visibleSeedContents(seeds);
        expect(
                bags.fit(GuideEngine.Kind.SEED, "Irit seed") == GuideEngine.Fit.YES,
                "existing stack fits in full box");
        expect(
                bags.fit(GuideEngine.Kind.SEED, "Lantadyme seed") == GuideEngine.Fit.NO,
                "seventh type cannot fit");
        v.seedBoxKnown = bags.seedsKnown;
        expect(
                engine.decide(v).action == GuideEngine.Action.CLOSE_SEED_BOX,
                "close instead of invisible Inventory tab");
        expect(engine.stage == GuideEngine.Stage.CLEANUP, "preserve cleanup stage");
        v.seedBoxOpen = false;
        v.inventoryVisible = true;
        v.inventory.add(
                new GuideEngine.Entry(
                        1,
                        1,
                        2,
                        "Irit seed",
                        GuideEngine.Kind.SELECTED_SEED,
                        bags.fit(GuideEngine.Kind.SEED, "Irit seed")));
        expect(
                engine.decide(v).action == GuideEngine.Action.SELECT_SEED,
                "resume individual seed use after closing");
        bags.request(GuideEngine.Kind.SEED, 21);
        bags.visibleSeedContents(seeds);
        expect(bags.seedsKnown, "repeat Check unchanged contents");
        bags.visibleSeedContents(Collections.emptyMap());
        expect(
                bags.fit(GuideEngine.Kind.SEED, "Lantadyme seed") == GuideEngine.Fit.YES,
                "empty box replaces previous contents");
        seeds.put("irit seed", Integer.MAX_VALUE);
        bags.visibleSeedContents(seeds);
        expect(
                bags.fit(GuideEngine.Kind.SEED, "Irit seed") == GuideEngine.Fit.NO,
                "full stack does not fit");
        bags.invalidate(GuideEngine.Kind.SEED);
        expect(
                bags.fit(GuideEngine.Kind.SEED, "Irit seed") == GuideEngine.Fit.UNKNOWN,
                "invalidation remains conservative");
        System.out.println("Seed box: 10 checks passed.");
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
