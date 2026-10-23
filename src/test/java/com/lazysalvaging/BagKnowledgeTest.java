package com.lazysalvaging;

import java.util.Map;

public final class BagKnowledgeTest {
    private static void expect(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    public static void main(String[] args) {
        BagKnowledge bags = new BagKnowledge();
        String gems =
                "Opal: 5 Jade: 8 Red topaz: 0 Sapphires: 0 Emeralds: 0 Rubies: 0 Diamonds: 60"
                    + " Dragonstones: 0";
        bags.message(gems, 1);
        expect(!bags.gemsKnown, "unsolicited messages do not establish contents");
        bags.request(GuideEngine.Kind.GEM, 2);
        bags.message(gems, 2);
        expect(bags.gemsKnown && bags.gems.get("uncut jade") == 8, "gem counts parsed");
        expect(
                bags.fit(GuideEngine.Kind.GEM, "uncut diamond") == GuideEngine.Fit.NO,
                "full gem type detected");

        bags.request(GuideEngine.Kind.HERB, 3);
        bags.message("You look in your herb sack and see:", 3);
        bags.message("<col=ffffff>6 x Grimy irit leaf</col>", 3);
        bags.message("3 x Grimy ranarr weed", 3);
        expect(!bags.herbsKnown, "herb response completes on the next tick");
        bags.tick(4);
        expect(
                bags.herbsKnown
                        && bags.herbs.get("grimy irit leaf") == 6
                        && bags.herbs.get("grimy ranarr weed") == 3,
                "herb lines combined");

        bags.request(GuideEngine.Kind.SEED, 5);
        bags.seedContents(Map.of("irit seed", 3, "poison ivy seed", 2), 5);
        expect(bags.seedsKnown && bags.seeds.get("irit seed") == 3, "seed contents read");
        bags.request(GuideEngine.Kind.SEED, 6);
        bags.message("The seed box contains:", 6);
        bags.message("2 x Irit seed.", 6);
        bags.tick(7);
        expect(
                bags.seedsKnown && bags.seeds.size() == 1 && bags.seeds.get("irit seed") == 2,
                "later contents replace the previous reading");

        bags.request(GuideEngine.Kind.GEM, 8);
        bags.message(gems, 21);
        expect(!bags.gemsKnown, "expired responses are ignored");
        bags.reset();
        expect(
                !bags.gemsKnown && !bags.herbsKnown && !bags.seedsKnown,
                "reset clears known contents");
        System.out.println("Bag knowledge: 9 checks passed.");
    }
}
