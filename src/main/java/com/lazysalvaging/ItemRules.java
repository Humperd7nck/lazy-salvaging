package com.lazysalvaging;

import java.util.*;

final class ItemRules {
    private static final Map<String, GuideEngine.Kind> RULES = new HashMap<>();

    static {
        RULES.put("facility bottle (empty)", GuideEngine.Kind.ALCHEMY);
        RULES.put("ironwood repair kit", GuideEngine.Kind.ALCHEMY);
        RULES.put("loop half of key", GuideEngine.Kind.ALCHEMY);
        RULES.put("tooth half of key", GuideEngine.Kind.ALCHEMY);
        RULES.put("rune spear", GuideEngine.Kind.ALCHEMY);
        RULES.put("shield left half", GuideEngine.Kind.ALCHEMY);
        RULES.put("dragon spear", GuideEngine.Kind.ALCHEMY);
        RULES.put("mouldy doll", GuideEngine.Kind.DROP);
        RULES.put("fish offcuts", GuideEngine.Kind.DROP);
        RULES.put("belladonna seed", GuideEngine.Kind.DROP);
        RULES.put("poison ivy seed", GuideEngine.Kind.DROP);
        RULES.put("cactus seed", GuideEngine.Kind.DROP);
        RULES.put("potato cactus seed", GuideEngine.Kind.DROP);
        RULES.put("silver ore", GuideEngine.Kind.DROP);
        RULES.put("silver bar", GuideEngine.Kind.DROP);
        RULES.put("tiara", GuideEngine.Kind.DROP);
        RULES.put("jug of wine", GuideEngine.Kind.DROP);
        RULES.put("spice", GuideEngine.Kind.DROP);
        RULES.put("grey wolf fur", GuideEngine.Kind.DROP);
        RULES.put("silk", GuideEngine.Kind.DROP);
        RULES.put("nature talisman", GuideEngine.Kind.DROP);
        RULES.put("rune javelin", GuideEngine.Kind.DROP);
        RULES.put("uncut opal", GuideEngine.Kind.GEM);
        RULES.put("uncut jade", GuideEngine.Kind.GEM);
        RULES.put("uncut red topaz", GuideEngine.Kind.GEM);
        RULES.put("uncut sapphire", GuideEngine.Kind.GEM);
        RULES.put("uncut emerald", GuideEngine.Kind.GEM);
        RULES.put("uncut ruby", GuideEngine.Kind.GEM);
        RULES.put("uncut diamond", GuideEngine.Kind.GEM);
        RULES.put("grimy avantoe", GuideEngine.Kind.HERB);
        RULES.put("grimy ranarr weed", GuideEngine.Kind.HERB);
        RULES.put("grimy snapdragon", GuideEngine.Kind.HERB);
        RULES.put("grimy torstol", GuideEngine.Kind.HERB);
        RULES.put("grimy kwuarm", GuideEngine.Kind.HERB);
        RULES.put("grimy dwarf weed", GuideEngine.Kind.HERB);
        RULES.put("grimy cadantine", GuideEngine.Kind.HERB);
        RULES.put("grimy lantadyme", GuideEngine.Kind.HERB);
        RULES.put("irit seed", GuideEngine.Kind.SEED);
        RULES.put("avantoe seed", GuideEngine.Kind.SEED);
        RULES.put("kwuarm seed", GuideEngine.Kind.SEED);
        RULES.put("cadantine seed", GuideEngine.Kind.SEED);
        RULES.put("lantadyme seed", GuideEngine.Kind.SEED);
        RULES.put("snape grass seed", GuideEngine.Kind.SEED);
        RULES.put("dwarf weed seed", GuideEngine.Kind.SEED);
        RULES.put("toadflax seed", GuideEngine.Kind.SEED);
    }

    static GuideEngine.Kind kind(String name) {
        return RULES.getOrDefault(name.toLowerCase(Locale.ROOT), GuideEngine.Kind.OTHER);
    }
}
