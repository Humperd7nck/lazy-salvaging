package com.lazysalvaging;

import net.runelite.client.config.ConfigItem;

import java.lang.reflect.Method;
import java.util.*;

public final class LootPolicyTest {
    private static int checks;

    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    private static GuideEngine.Entry item(String name, GuideEngine.Kind kind, GuideEngine.Fit fit) {
        return new GuideEngine.Entry(3, 1, 1, name, kind, fit);
    }

    private static GuideEngine cleaning() {
        GuideEngine g = new GuideEngine();
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.CLEANUP;
        return g;
    }

    public static void main(String[] args) throws Exception {
        LazySalvagingConfig defaults = new LazySalvagingConfig() {};
        expect(!defaults.dropOverflow(), "overflow opt-in");
        expect(LootPolicy.kind("Coins", defaults) == GuideEngine.Kind.OTHER, "unknown items kept");
        int count = 0, headers = 0;
        Set<Integer> positions = new HashSet<>();
        for (Method m : LazySalvagingConfig.class.getMethods()) {
            ConfigItem c = m.getAnnotation(ConfigItem.class);
            if (c == null || !c.section().equals(LazySalvagingConfig.lootSection)) continue;
            expect(positions.add(c.position()), "loot positions unique");
            if (m.getReturnType() == void.class) {
                headers++;
                continue;
            }
            if (m.getReturnType() != LootAction.class
                    && m.getReturnType() != StoredLootAction.class) continue;
            count++;
            GuideEngine.Kind old = SalvageSeed.classify(c.name(), Collections.emptySet());
            expect(
                    LootPolicy.kind(c.name(), defaults) == old,
                    "default behavior preserved for " + c.name());
        }
        expect(
                count == 52 && headers == 4,
                "one continuous section: 52 dropdowns, four plain headers");
        for (String key : new String[] {"cargoSwap", "dropSwap", "preventBoatMisclicks"})
            expect(
                    LazySalvagingConfig.class
                            .getMethod(key)
                            .getAnnotation(ConfigItem.class)
                            .hidden(),
                    "legacy toggle hidden");
        expect(
                !new LazySalvagingConfig() {
                    public boolean cargoSwap() {
                        return false;
                    }

                    public boolean dropSwap() {
                        return false;
                    }
                }.menuSwap(),
                "master inherits all-off preferences");
        LazySalvagingConfig custom =
                new LazySalvagingConfig() {
                    public LootAction lootTiara() {
                        return LootAction.KEEP;
                    }

                    public LootAction lootDragonNails() {
                        return LootAction.ALCH;
                    }

                    public StoredLootAction lootUncutRuby() {
                        return StoredLootAction.DROP;
                    }

                    public StoredLootAction lootGrimyTorstol() {
                        return StoredLootAction.KEEP;
                    }

                    public boolean seedBelladonna() {
                        return true;
                    }
                };
        expect(LootPolicy.kind("Tiara", custom) == GuideEngine.Kind.OTHER, "custom Keep");
        expect(LootPolicy.kind("Dragon nails", custom) == GuideEngine.Kind.ALCHEMY, "custom Alch");
        expect(LootPolicy.kind("Uncut ruby", custom) == GuideEngine.Kind.DROP, "custom Drop");
        expect(LootPolicy.kind("Grimy torstol", custom) == GuideEngine.Kind.OTHER, "herb Keep");
        expect(
                LootPolicy.kind("Belladonna seed", custom) == GuideEngine.Kind.SELECTED_SEED,
                "seed selection takes priority");
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.inventoryVisible = true;
        v.wreckAvailable = true;
        v.cargoKnown = true;
        v.cargoSlots = 10;
        for (GuideEngine.Kind kind :
                new GuideEngine.Kind[] {
                    GuideEngine.Kind.GEM, GuideEngine.Kind.HERB, GuideEngine.Kind.SELECTED_SEED
                }) {
            GuideEngine.Entry full = item("overflow", kind, GuideEngine.Fit.NO);
            v.dropOverflow = false;
            v.gemsPresent = v.herbsPresent = v.seedsPresent = true;
            expect(!LootPolicy.drop(full, v), "overflow kept while disabled");
            v.dropOverflow = true;
            expect(LootPolicy.drop(full, v), "confirmed full drops with opt-in");
            expect(
                    !LootPolicy.drop(item("unknown", kind, GuideEngine.Fit.UNKNOWN), v),
                    "unknown never dropped");
            v.gemsPresent = v.herbsPresent = v.seedsPresent = false;
            expect(!LootPolicy.drop(full, v), "missing container cannot authorize overflow");
        }
        expect(
                !LootPolicy.drop(item("Keep", GuideEngine.Kind.OTHER, GuideEngine.Fit.NO), v),
                "Keep always protected");
        expect(
                LootPolicy.drop(item("Drop", GuideEngine.Kind.DROP, GuideEngine.Fit.UNKNOWN), v),
                "explicit Drop needs no container");
        v.gemsPresent = true;
        v.dropOverflow = false;
        v.inventory.add(item("Uncut ruby", GuideEngine.Kind.GEM, GuideEngine.Fit.NO));
        expect(
                cleaning().decide(v).action == GuideEngine.Action.DEPLOY,
                "full store items kept without blocking cleanup");
        v.dropOverflow = true;
        expect(
                cleaning().decide(v).action == GuideEngine.Action.DROP_ITEM,
                "engine honors overflow setting");
        v.inventory.clear();
        v.inventory.add(item("Uncut ruby", GuideEngine.Kind.GEM, GuideEngine.Fit.YES));
        expect(
                cleaning().decide(v).action == GuideEngine.Action.FILL_GEMS,
                "all Store gems use Fill");
        v.inventory.add(
                new GuideEngine.Entry(
                        4, 2, 1, "Uncut diamond", GuideEngine.Kind.OTHER, GuideEngine.Fit.UNKNOWN));
        expect(
                cleaning().decide(v).action == GuideEngine.Action.SELECT_STORE_ITEM,
                "mixed Keep inventory selects only Store item");
        v.selectedStoreSlot = 3;
        expect(
                cleaning().decide(v).action == GuideEngine.Action.USE_GEM_BAG,
                "selected Store item targets correct bag");
        v.inventory.clear();
        v.seedsPresent = true;
        v.seedDepositBlocked = true;
        v.inventory.add(item("Irit seed", GuideEngine.Kind.SELECTED_SEED, GuideEngine.Fit.UNKNOWN));
        expect(
                cleaning().decide(v).action == GuideEngine.Action.CHECK_SEEDS,
                "failed transfer alone never means full");
        v.inventory.set(0, item("Irit seed", GuideEngine.Kind.SELECTED_SEED, GuideEngine.Fit.NO));
        expect(
                cleaning().decide(v).action == GuideEngine.Action.DROP_ITEM,
                "checked seed overflow honors opt-in");
        v.dropOverflow = false;
        expect(
                cleaning().decide(v).action == GuideEngine.Action.DEPLOY,
                "checked seed overflow kept without blocking");
        BagKnowledge bags = new BagKnowledge();
        bags.gemsKnown = true;
        bags.gems.put("uncut ruby", 59);
        BagFillTracker tracker = new BagFillTracker();
        Map<String, Integer> before = new HashMap<>();
        before.put("uncut ruby", 3);
        before.put("uncut diamond", 1);
        tracker.beginSingle(GuideEngine.Kind.GEM, "Uncut ruby", before, bags, 0);
        Map<String, Integer> after = new HashMap<>(before);
        after.put("uncut ruby", 2);
        expect(
                tracker.observe(after, bags, 1) == BagFillTracker.Result.CONFIRMED,
                "single-item storage updates remembered capacity");
        expect(
                bags.gems.get("uncut ruby") == 60 && bags.gemsKnown,
                "partial storage reaches capacity");
        bags.gems.put("uncut ruby", 0);
        tracker.beginSingle(GuideEngine.Kind.GEM, "Uncut ruby", before, bags, 2);
        after.remove("uncut diamond");
        expect(
                tracker.observe(after, bags, 3) == BagFillTracker.Result.UNCERTAIN,
                "unrelated item change never inferred as storage");
        System.out.println("Loot policy: " + checks + " checks passed.");
    }
}
