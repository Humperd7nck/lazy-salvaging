package com.lazysalvaging;

import java.util.*;

public final class GuideEngineTest {
    static int checks;

    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }

    static GuideEngine.View view() {
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.dropOverflow = true;
        v.wreckAvailable = true;
        v.inventoryVisible = true;
        v.gemsPresent = v.herbsPresent = v.seedsPresent = true;
        return v;
    }

    static GuideEngine.Entry entry(int slot, String name, GuideEngine.Fit fit) {
        return new GuideEngine.Entry(slot, slot + 1, 1, name, ItemRules.kind(name), fit);
    }

    static GuideEngine cleaning() {
        GuideEngine g = new GuideEngine();
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.CLEANUP;
        g.assignRequired = false;
        return g;
    }

    static void action(
            GuideEngine g, GuideEngine.View v, GuideEngine.Action expected, String label) {
        check(g.decide(v).action == expected, label);
    }

    public static void main(String[] args) {
        GuideEngine g = new GuideEngine();
        GuideEngine.View v = view();
        action(g, v, GuideEngine.Action.OPEN_CARGO, "unknown cargo needs opening");
        v.cargoOpen = true;
        action(g, v, GuideEngine.Action.NONE, "unknown not treated as zero");
        v.cargoFresh = true;
        v.cargoSlots = 209;
        v.cargoSalvage = 200;
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "209 stays active");
        check(g.mode == GuideEngine.Mode.ACTIVE, "active initialized");
        v.cargoOpen = false;
        v.cargoSlots = 180;
        v.occupied = 28;
        v.salvage = 20;
        action(
                g,
                v,
                GuideEngine.Action.DEPOSIT,
                "full inventory below projected threshold deposits");
        g.deposited();
        v.cargoFresh = false;
        v.occupied = 8;
        v.salvage = 0;
        action(g, v, GuideEngine.Action.OPEN_CARGO, "confirmed deposit requires fresh count");
        v.cargoOpen = true;
        v.cargoFresh = true;
        v.cargoSlots = 210;
        v.cargoSalvage = 190;
        action(g, v, GuideEngine.Action.WITHDRAW, "210 enters sorting and tops up");
        check(g.mode == GuideEngine.Mode.SORTING, "sorting initialized");
        v.occupied = 28;
        v.salvage = 20;
        v.cargoSlots = 190;
        v.cargoSalvage = 170;
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "sorting stays locked below 210");
        v.cargoOpen = false;
        action(g, v, GuideEngine.Action.SAILING_TAB, "assign starts at sailing tab");
        v.sailingVisible = true;
        v.facilitiesVisible = true;
        action(g, v, GuideEngine.Action.ASSIGN_HOOK, "hook assign");
        v.assignmentPicker = true;
        action(g, v, GuideEngine.Action.JENKINS, "Jenkins selected");
        v.crewAssigned = true;
        action(g, v, GuideEngine.Action.SORT, "confirmed assignment advances");
        v.sorting = true;
        v.cargoSalvage = 0;
        action(g, v, GuideEngine.Action.NONE, "cargo falling during sort cannot exit");
        check(g.mode == GuideEngine.Mode.SORTING, "sorting remains locked");
        v.sorting = false;
        v.salvage = 0;
        v.inventory.add(entry(2, "Uncut ruby", GuideEngine.Fit.UNKNOWN));
        action(g, v, GuideEngine.Action.CHECK_GEMS, "unknown capacity requires check");
        v.inventory.set(0, entry(2, "Uncut ruby", GuideEngine.Fit.YES));
        action(g, v, GuideEngine.Action.FILL_GEMS, "space permits fill");
        v.inventory.set(0, entry(2, "Uncut ruby", GuideEngine.Fit.NO));
        action(g, v, GuideEngine.Action.DROP_ITEM, "known full permits drop");
        v.inventory.clear();
        v.cargoOpen = false;
        v.cargoFresh = false;
        action(g, v, GuideEngine.Action.OPEN_CARGO, "cleanup done checks cargo");
        v.cargoOpen = true;
        v.cargoFresh = true;
        v.cargoSlots = 15;
        v.cargoSalvage = 10;
        v.occupied = 8;
        action(g, v, GuideEngine.Action.WITHDRAW, "15 repeats sorting");
        v.occupied = 23;
        v.salvage = 15;
        v.cargoSalvage = 0;
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "partial withdrawal completes at empty cargo");
        v.cargoOpen = false;
        v.crewAssigned = false;
        action(g, v, GuideEngine.Action.SORT, "does not repeat assignment each inventory");
        v.salvage = 0;
        v.cargoOpen = true;
        v.cargoFresh = true;
        v.cargoSlots = 14;
        v.cargoSalvage = 0;
        // PROCESS first requires closing an unexpectedly open interface.
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "close interface before process");
        v.cargoOpen = false;
        v.cargoFresh = false;
        action(g, v, GuideEngine.Action.OPEN_CARGO, "fresh postcleanup check");
        v.cargoOpen = true;
        v.cargoFresh = true;
        action(g, v, GuideEngine.Action.CLOSE_CARGO, "14 returns active");
        check(g.mode == GuideEngine.Mode.ACTIVE, "exit mode active");
        g = cleaning();
        v = view();
        v.inventory.add(entry(0, "Coins", GuideEngine.Fit.UNKNOWN));
        v.inventory.add(entry(1, "Tiara", GuideEngine.Fit.UNKNOWN));
        action(g, v, GuideEngine.Action.DROP_ITEM, "tiara remains drop");
        check(g.decide(v).slot == 1, "unknown items untouched");
        v.inventory.add(entry(2, "Rune spear", GuideEngine.Fit.UNKNOWN));
        v.alchBlocked = "no runes";
        action(g, v, GuideEngine.Action.NONE, "missing runes pauses before drops");
        v.alchBlocked = "";
        action(g, v, GuideEngine.Action.MAGIC_TAB, "magic tab first");
        v.magicVisible = true;
        action(g, v, GuideEngine.Action.HIGH_ALCH, "spell selected before item");
        v.alchSelected = true;
        action(g, v, GuideEngine.Action.ALCH_ITEM, "one alch target after selection");
        check(g.decide(v).slot == 2, "correct alchemy slot");
        v.alchSelected = false;
        v.magicVisible = false;
        v.alchReturning = true;
        action(
                g,
                v,
                GuideEngine.Action.NONE,
                "spellbook return never prompts Magic tab between casts");
        v.alchReturning = false;
        v.magicVisible = true;
        action(g, v, GuideEngine.Action.HIGH_ALCH, "returned spellbook highlights spell directly");
        v.inventory.remove(2);
        v.alchReturning = true;
        v.magicVisible = false;
        action(
                g,
                v,
                GuideEngine.Action.NONE,
                "last cast waits for spellbook return before cleanup");
        v.alchReturning = false;
        v.magicVisible = true;
        action(g, v, GuideEngine.Action.INVENTORY_TAB, "return from spellbook after final alch");
        g = cleaning();
        v = view();
        v.inventory.add(
                new GuideEngine.Entry(
                        0,
                        5297,
                        1,
                        "Irit seed",
                        GuideEngine.Kind.SELECTED_SEED,
                        GuideEngine.Fit.UNKNOWN));
        v.inventory.add(entry(3, "Silk", GuideEngine.Fit.UNKNOWN));
        action(g, v, GuideEngine.Action.DROP_ITEM, "drop list before seed fill");
        v.inventory.remove(1);
        action(g, v, GuideEngine.Action.SELECT_SEED, "selected seed uses box without checking");
        v.inventory.clear();
        v.inventory.add(entry(0, "Uncut opal", GuideEngine.Fit.UNKNOWN));
        v.gemsPresent = false;
        action(
                g,
                v,
                GuideEngine.Action.OPEN_CARGO,
                "missing bag keeps items and continues cleanup");
        g = new GuideEngine();
        g.mode = GuideEngine.Mode.ACTIVE;
        g.cargoRequired = false;
        v = view();
        v.cargoKnown = true;
        v.cargoSlots = 100;
        v.depleted = true;
        v.salvaging = true;
        action(
                g,
                v,
                GuideEngine.Action.DEPLOY,
                "depletion overrides stale activity and highlights hook");
        v.depleted = false;
        v.salvaging = true;
        check(g.decide(v).preview == GuideEngine.Action.DEPOSIT, "busy salvaging previews deposit");
        BagKnowledge b = new BagKnowledge();
        b.message(
                "Opal: 60 / Jade: 1 / Red Topaz: 0Sapphires: 0 / Emeralds: 0 / Rubies: 0Diamonds: 0"
                    + " / Dragonstones: 0",
                0);
        check(!b.gemsKnown, "unsolicited chat cannot set capacity");
        b.request(GuideEngine.Kind.GEM, 1);
        b.message(
                "Opal: 60 / Jade: 29 / Red Topaz: 23Sapphires: 3 / Emeralds: 1 / Rubies: 0Diamonds:"
                    + " 0 / Dragonstones: 0",
                1);
        check(
                b.fit(GuideEngine.Kind.GEM, "Uncut opal") == GuideEngine.Fit.NO,
                "full gem type recognized");
        check(
                b.fit(GuideEngine.Kind.GEM, "Uncut jade") == GuideEngine.Fit.YES,
                "other gem still fits");
        b.request(GuideEngine.Kind.HERB, 2);
        b.message("You look in your herb sack and see:", 2);
        b.message("100 x Grimy ranarr weed", 2);
        b.message("33 x Grimy irit leaf", 2);
        b.tick(3);
        check(
                b.fit(GuideEngine.Kind.HERB, "Grimy ranarr weed") == GuideEngine.Fit.NO,
                "full herb type");
        check(
                b.fit(GuideEngine.Kind.HERB, "Grimy torstol") == GuideEngine.Fit.YES,
                "absent herb count zero after complete check");
        Map<String, Integer> seeds = new HashMap<>();
        for (String name :
                new String[] {
                    "irit seed",
                    "toadflax seed",
                    "poison ivy seed",
                    "avantoe seed",
                    "kwuarm seed",
                    "cadantine seed"
                }) seeds.put(name, 1);
        b.request(GuideEngine.Kind.SEED, 4);
        b.seedContents(seeds, 4);
        check(
                b.fit(GuideEngine.Kind.SEED, "Lantadyme seed") == GuideEngine.Fit.NO,
                "six seed types blocks new type");
        check(
                b.fit(GuideEngine.Kind.SEED, "Irit seed") == GuideEngine.Fit.YES,
                "existing seed type still fits");
        b.invalidate(GuideEngine.Kind.GEM);
        check(
                b.fit(GuideEngine.Kind.GEM, "Uncut opal") == GuideEngine.Fit.UNKNOWN,
                "fill invalidates capacity for recheck");
        SalvagingActivity activity = new SalvagingActivity();
        check(!activity.observe(-1), "idle startup is not salvaging");
        check(activity.observe(13583), "deployment animation starts activity");
        check(activity.observe(13584), "salvaging animation stays active");
        check(activity.observe(-1), "single idle tick tolerates animation gap");
        check(!activity.observe(-1), "stopped animation expires activity");
        activity.observe(13584);
        activity.reset();
        check(!activity.observe(-1), "depletion clears prior activity");
        check(activity.observe(13583), "new deployment restarts activity");
        System.out.println("Guide tests: " + checks + " checks passed.");
    }
}
