package com.lazysalvaging;

import java.util.*;

/** Correlates a user Fill with the exact expected inventory transfer. */
final class BagFillTracker {
    enum Result {
        IDLE,
        WAITING,
        CONFIRMED,
        UNCERTAIN
    }

    private GuideEngine.Kind kind = GuideEngine.Kind.OTHER;
    private Map<String, Integer> before, expected, deposits;
    private int started;
    private String singleName;

    boolean active() {
        return kind != GuideEngine.Kind.OTHER;
    }

    void cancel(BagKnowledge bags) {
        if (active()) bags.invalidate(kind);
        clear();
    }

    void clear() {
        kind = GuideEngine.Kind.OTHER;
        before = expected = deposits = null;
        singleName = null;
    }

    void beginSingle(
            GuideEngine.Kind bag,
            String name,
            Map<String, Integer> inventory,
            BagKnowledge bags,
            int tick) {
        begin(bag, inventory, bags, tick);
        if (active()) singleName = name.toLowerCase(Locale.ROOT);
    }

    void begin(GuideEngine.Kind bag, Map<String, Integer> inventory, BagKnowledge bags, int tick) {
        cancel(bags);
        if (bag != GuideEngine.Kind.GEM && bag != GuideEngine.Kind.HERB) return;
        boolean known = bag == GuideEngine.Kind.GEM ? bags.gemsKnown : bags.herbsKnown;
        if (!known) return;
        kind = bag;
        started = tick;
        before = new HashMap<>(inventory);
        expected = new HashMap<>(inventory);
        deposits = new HashMap<>();
        Map<String, Integer> contents = bag == GuideEngine.Kind.GEM ? bags.gems : bags.herbs;
        int capacity = bag == GuideEngine.Kind.GEM ? 60 : 100;
        inventory.forEach(
                (name, count) -> {
                    if (!accepts(bag, name)) return;
                    int amount =
                            Math.min(count, Math.max(0, capacity - contents.getOrDefault(name, 0)));
                    if (amount > 0) {
                        deposits.put(name, amount);
                        if (amount == count) expected.remove(name);
                        else expected.put(name, count - amount);
                    }
                });
    }

    Result observe(Map<String, Integer> inventory, BagKnowledge bags, int tick) {
        if (!active()) return Result.IDLE;
        if (tick <= started) return Result.WAITING;
        if (!inventory.equals(before)) {
            if (singleName != null) {
                int amount =
                        before.getOrDefault(singleName, 0) - inventory.getOrDefault(singleName, 0);
                Map<String, Integer> exact = new HashMap<>(before);
                if (amount == before.getOrDefault(singleName, 0)) exact.remove(singleName);
                else exact.put(singleName, before.getOrDefault(singleName, 0) - amount);
                if (amount <= 0
                        || amount > deposits.getOrDefault(singleName, 0)
                        || !exact.equals(inventory)) {
                    cancel(bags);
                    return Result.UNCERTAIN;
                }
                boolean applied =
                        bags.addDeposits(kind, Collections.singletonMap(singleName, amount));
                clear();
                return applied ? Result.CONFIRMED : Result.UNCERTAIN;
            }
            if (!inventory.equals(expected)) {
                cancel(bags);
                return Result.UNCERTAIN;
            }
            boolean applied = bags.addDeposits(kind, deposits);
            clear();
            return applied ? Result.CONFIRMED : Result.UNCERTAIN;
        }
        if (tick - started > 6) {
            cancel(bags);
            return Result.UNCERTAIN;
        }
        return Result.WAITING;
    }

    static boolean accepts(GuideEngine.Kind kind, String name) {
        if (kind == GuideEngine.Kind.GEM)
            return Arrays.asList(
                            "uncut opal",
                            "uncut jade",
                            "uncut red topaz",
                            "uncut sapphire",
                            "uncut emerald",
                            "uncut ruby",
                            "uncut diamond",
                            "uncut dragonstone")
                    .contains(name);
        if (kind == GuideEngine.Kind.HERB)
            return Arrays.asList(
                            "grimy guam leaf",
                            "grimy marrentill",
                            "grimy tarromin",
                            "grimy harralander",
                            "grimy ranarr weed",
                            "grimy toadflax",
                            "grimy irit leaf",
                            "grimy avantoe",
                            "grimy kwuarm",
                            "grimy snapdragon",
                            "grimy cadantine",
                            "grimy lantadyme",
                            "grimy dwarf weed",
                            "grimy torstol")
                    .contains(name);
        return false;
    }
}
