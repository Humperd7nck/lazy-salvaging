package com.lazysalvaging;

import java.util.*;

public final class BagFillTrackerTest {
    private static int checks;

    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    private static Map<String, Integer> inventory(int gems, int herbs) {
        Map<String, Integer> result = new HashMap<>();
        result.put("open gem sack", 1);
        result.put("open silklined herb sack", 1);
        result.put("coins", 100);
        if (gems > 0) result.put("uncut jade", gems);
        if (herbs > 0) result.put("grimy avantoe", herbs);
        return result;
    }

    public static void main(String[] args) {
        BagKnowledge b = new BagKnowledge();
        BagFillTracker t = new BagFillTracker();
        b.gemsKnown = b.herbsKnown = true;
        b.gems.put("uncut jade", 58);
        b.herbs.put("grimy avantoe", 97);
        t.begin(GuideEngine.Kind.GEM, inventory(5, 4), b, 0);
        expect(
                t.observe(inventory(5, 4), b, 1) == BagFillTracker.Result.WAITING,
                "wait for transfer");
        expect(
                t.observe(inventory(3, 4), b, 2) == BagFillTracker.Result.CONFIRMED,
                "partial deposit to gem capacity");
        expect(b.gems.get("uncut jade") == 60 && b.gemsKnown, "retain and increment gem contents");
        expect(
                b.fit(GuideEngine.Kind.GEM, "uncut jade") == GuideEngine.Fit.NO,
                "remaining jade now overflow");
        expect(
                t.observe(inventory(3, 4), b, 3) == BagFillTracker.Result.IDLE
                        && b.gems.get("uncut jade") == 60,
                "do not double count");
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 4);
        expect(
                t.observe(inventory(3, 1), b, 5) == BagFillTracker.Result.CONFIRMED,
                "partial herb deposit");
        expect(b.herbs.get("grimy avantoe") == 100 && b.herbsKnown, "herb count reaches capacity");
        expect(b.gemsKnown, "herb fill preserves gem knowledge");
        b.herbs.clear();
        t.begin(GuideEngine.Kind.HERB, inventory(3, 2), b, 6);
        expect(
                t.observe(inventory(3, 0), b, 7) == BagFillTracker.Result.CONFIRMED,
                "new herb type");
        expect(b.herbs.get("grimy avantoe") == 2, "new type count exact");
        t.begin(GuideEngine.Kind.HERB, inventory(3, 5), b, 8);
        expect(
                t.observe(inventory(3, 0), b, 9) == BagFillTracker.Result.CONFIRMED
                        && b.herbs.get("grimy avantoe") == 7,
                "second cleanup accumulates");
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 10);
        Map<String, Integer> changed = inventory(3, 0);
        changed.put("coins", 101);
        expect(
                t.observe(changed, b, 11) == BagFillTracker.Result.UNCERTAIN,
                "unrelated change rejected");
        expect(
                !b.herbsKnown && b.herbs.get("grimy avantoe") == 7,
                "uncertainty does not guess deposit");
        expect(b.gemsKnown, "uncertainty scoped to affected bag");
        b.herbsKnown = true;
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 12);
        t.cancel(b);
        expect(!b.herbsKnown && !t.active(), "interrupted fill invalidates");
        b.herbsKnown = true;
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 13);
        expect(
                t.observe(inventory(3, 4), b, 20) == BagFillTracker.Result.UNCERTAIN
                        && !b.herbsKnown,
                "timeout requests check");
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 21);
        expect(!t.active(), "cannot invent initial contents");
        b.herbsKnown = true;
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 22);
        expect(
                t.observe(inventory(3, 2), b, 23) == BagFillTracker.Result.UNCERTAIN,
                "unexpected removal amount rejected");
        b.herbsKnown = true;
        t.begin(GuideEngine.Kind.HERB, inventory(3, 4), b, 24);
        b.invalidate(GuideEngine.Kind.HERB);
        expect(
                t.observe(inventory(3, 0), b, 25) == BagFillTracker.Result.UNCERTAIN,
                "lost baseline stays unknown");
        t.begin(GuideEngine.Kind.SEED, inventory(3, 4), b, 26);
        expect(!t.active(), "seed box excluded");
        expect(
                !BagFillTracker.accepts(GuideEngine.Kind.GEM, "noted:uncut jade"),
                "noted gems excluded");
        expect(
                BagFillTracker.accepts(GuideEngine.Kind.HERB, "grimy irit leaf"),
                "track herbs outside cleanup list too");
        b.request(GuideEngine.Kind.HERB, 27);
        b.message("You look in your herb sack and see:", 27);
        b.message("35 x Grimy avantoe", 27);
        b.tick(28);
        expect(b.herbsKnown && b.herbs.get("grimy avantoe") == 35, "fresh Check replaces estimate");
        b.gemsKnown = true;
        b.gems.put("uncut jade", 59);
        b.gems.put("uncut ruby", 12);
        Map<String, Integer> mixed = inventory(3, 0);
        mixed.put("uncut ruby", 4);
        t.begin(GuideEngine.Kind.GEM, mixed, b, 30);
        expect(
                t.observe(inventory(2, 0), b, 31) == BagFillTracker.Result.CONFIRMED,
                "multiple gem types in one Fill");
        expect(
                b.gems.get("uncut jade") == 60 && b.gems.get("uncut ruby") == 16,
                "independent per-type counts");
        System.out.println("Bag fill tracker: " + checks + " checks passed.");
    }
}
