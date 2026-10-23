package com.lazysalvaging;

import java.util.*;

/** Seed types on the Opulent salvage drop table. */
public enum SalvageSeed {
    AVANTOE("Avantoe seed"),
    BELLADONNA("Belladonna seed"),
    CACTUS("Cactus seed"),
    CADANTINE("Cadantine seed"),
    DWARF_WEED("Dwarf weed seed"),
    IRIT("Irit seed"),
    KWUARM("Kwuarm seed"),
    LANTADYME("Lantadyme seed"),
    POISON_IVY("Poison ivy seed"),
    POTATO_CACTUS("Potato cactus seed"),
    SNAPDRAGON("Snapdragon seed"),
    SNAPE_GRASS("Snape grass seed"),
    TOADFLAX("Toadflax seed"),
    TORSTOL("Torstol seed");
    public final String itemName;

    SalvageSeed(String name) {
        itemName = name;
    }

    @Override
    public String toString() {
        return itemName;
    }

    static GuideEngine.Kind classify(String name, Set<SalvageSeed> selected) {
        for (SalvageSeed seed : values())
            if (seed.itemName.equalsIgnoreCase(name))
                return selected.contains(seed)
                        ? GuideEngine.Kind.SELECTED_SEED
                        : ItemRules.kind(name) == GuideEngine.Kind.DROP
                                ? GuideEngine.Kind.DROP
                                : GuideEngine.Kind.OTHER;
        return ItemRules.kind(name);
    }
}
