package com.lazysalvaging;

import java.util.*;
import java.util.regex.*;

/** Check responses are accepted only within a matching user-initiated Check request. */
final class BagKnowledge {
    final Map<String, Integer> gems = new HashMap<>(),
            herbs = new HashMap<>(),
            seeds = new HashMap<>();
    boolean gemsKnown, herbsKnown, seedsKnown;
    private GuideEngine.Kind pending = GuideEngine.Kind.OTHER;
    private int requestedTick, headerTick = -1;
    private final Map<String, Integer> pendingHerbs = new HashMap<>();

    void reset() {
        gemsKnown = herbsKnown = seedsKnown = false;
        pending = GuideEngine.Kind.OTHER;
        headerTick = -1;
    }

    void invalidate(GuideEngine.Kind kind) {
        if (kind == GuideEngine.Kind.GEM) gemsKnown = false;
        if (kind == GuideEngine.Kind.HERB) herbsKnown = false;
        if (kind == GuideEngine.Kind.SEED) seedsKnown = false;
    }

    void request(GuideEngine.Kind kind, int tick) {
        invalidate(kind);
        pending = kind;
        requestedTick = tick;
        headerTick = -1;
        pendingHerbs.clear();
    }

    void tick(int tick) {
        if ((pending == GuideEngine.Kind.HERB || pending == GuideEngine.Kind.SEED)
                && headerTick >= 0
                && tick > headerTick) {
            if (pending == GuideEngine.Kind.HERB) {
                herbs.clear();
                herbs.putAll(pendingHerbs);
                herbsKnown = true;
            } else {
                seeds.clear();
                seeds.putAll(pendingHerbs);
                seedsKnown = true;
            }
            pending = GuideEngine.Kind.OTHER;
        }
        if (tick - requestedTick > 12) pending = GuideEngine.Kind.OTHER;
    }

    boolean awaiting(GuideEngine.Kind kind, int tick) {
        return pending == kind && tick - requestedTick <= 12;
    }

    void message(String message, int tick) {
        if (tick - requestedTick > 12) return;
        String s =
                message.replaceAll("<br\\s*/?>", " ")
                        .replaceAll("<[^>]*>", "")
                        .toLowerCase(Locale.ROOT);
        if (pending == GuideEngine.Kind.SEED) {
            if (s.contains("the seed box contains:")) {
                headerTick = tick;
                pendingHerbs.clear();
                return;
            }
            if (headerTick >= 0) {
                Matcher m = Pattern.compile("([0-9,]+) x ([a-z ]+seed)\\.?").matcher(s);
                if (m.find())
                    pendingHerbs.put(
                            m.group(2).trim(), Integer.parseInt(m.group(1).replace(",", "")));
            }
            if (s.contains("seed box is empty")) {
                seeds.clear();
                seedsKnown = true;
                pending = GuideEngine.Kind.OTHER;
            }
        }
        if (pending == GuideEngine.Kind.HERB) {
            if (s.contains("you look in your herb sack and see")) {
                headerTick = tick;
                pendingHerbs.clear();
                return;
            }
            if (headerTick >= 0) {
                Matcher m = Pattern.compile("([0-9,]+) x (grimy [a-z ]+)").matcher(s);
                if (m.find())
                    pendingHerbs.put(
                            m.group(2).trim(), Integer.parseInt(m.group(1).replace(",", "")));
            }
            if (s.contains("herb sack is empty") || s.contains("herb sack is currently empty")) {
                herbs.clear();
                herbsKnown = true;
                pending = GuideEngine.Kind.OTHER;
            }
        }
        if (pending == GuideEngine.Kind.GEM) {
            Map<String, Integer> found = new HashMap<>();
            Matcher m =
                    Pattern.compile(
                                    "(opal|jade|red"
                                        + " topaz|sapphires|emeralds|rubies|diamonds|dragonstones):\\s*([0-9,]+)")
                            .matcher(s);
            while (m.find()) {
                String name = m.group(1);
                if (name.equals("rubies")) name = "ruby";
                else if (name.endsWith("s")) name = name.substring(0, name.length() - 1);
                found.put("uncut " + name, Integer.parseInt(m.group(2).replace(",", "")));
            }
            if (found.size() == 8) {
                gems.clear();
                gems.putAll(found);
                gemsKnown = true;
                pending = GuideEngine.Kind.OTHER;
            }
        }
    }

    void seedContents(Map<String, Integer> contents, int tick) {
        if (awaiting(GuideEngine.Kind.SEED, tick)) {
            seeds.clear();
            seeds.putAll(contents);
            seedsKnown = true;
            pending = GuideEngine.Kind.OTHER;
        }
    }

    // An open contents interface is authoritative, even without a new container event.
    void visibleSeedContents(Map<String, Integer> contents) {
        seeds.clear();
        seeds.putAll(contents);
        seedsKnown = true;
        if (pending == GuideEngine.Kind.SEED) pending = GuideEngine.Kind.OTHER;
    }

    boolean addDeposits(GuideEngine.Kind kind, Map<String, Integer> deposits) {
        Map<String, Integer> contents = kind == GuideEngine.Kind.GEM ? gems : herbs;
        if (kind != GuideEngine.Kind.GEM && kind != GuideEngine.Kind.HERB) return false;
        if (!(kind == GuideEngine.Kind.GEM ? gemsKnown : herbsKnown)) return false;
        int capacity = kind == GuideEngine.Kind.GEM ? 60 : 100;
        for (Map.Entry<String, Integer> deposit : deposits.entrySet()) {
            int amount = deposit.getValue();
            if (!BagFillTracker.accepts(kind, deposit.getKey())
                    || amount <= 0
                    || amount > capacity - contents.getOrDefault(deposit.getKey(), 0)) {
                invalidate(kind);
                return false;
            }
        }
        deposits.forEach((name, amount) -> contents.merge(name, amount, Integer::sum));
        return true;
    }

    GuideEngine.Fit fit(GuideEngine.Kind kind, String name) {
        String n = name.toLowerCase(Locale.ROOT);
        if (kind == GuideEngine.Kind.GEM)
            return gemsKnown
                    ? (gems.getOrDefault(n, 0) >= 60 ? GuideEngine.Fit.NO : GuideEngine.Fit.YES)
                    : GuideEngine.Fit.UNKNOWN;
        if (kind == GuideEngine.Kind.HERB)
            return herbsKnown
                    ? (herbs.getOrDefault(n, 0) >= 100 ? GuideEngine.Fit.NO : GuideEngine.Fit.YES)
                    : GuideEngine.Fit.UNKNOWN;
        if (kind == GuideEngine.Kind.SEED || kind == GuideEngine.Kind.SELECTED_SEED) {
            if (!seedsKnown) return GuideEngine.Fit.UNKNOWN;
            if (seeds.containsKey(n))
                return seeds.get(n) == Integer.MAX_VALUE ? GuideEngine.Fit.NO : GuideEngine.Fit.YES;
            return seeds.size() >= 6 ? GuideEngine.Fit.NO : GuideEngine.Fit.YES;
        }
        return GuideEngine.Fit.UNKNOWN;
    }
}
