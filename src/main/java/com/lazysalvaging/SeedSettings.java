package com.lazysalvaging;

import net.runelite.client.config.ConfigManager;

import java.util.*;

final class SeedSettings {
    static String key(SalvageSeed seed) {
        switch (seed) {
            case AVANTOE:
                return "seedAvantoe";
            case BELLADONNA:
                return "seedBelladonna";
            case CACTUS:
                return "seedCactus";
            case CADANTINE:
                return "seedCadantine";
            case DWARF_WEED:
                return "seedDwarfWeed";
            case IRIT:
                return "seedIrit";
            case KWUARM:
                return "seedKwuarm";
            case LANTADYME:
                return "seedLantadyme";
            case POISON_IVY:
                return "seedPoisonIvy";
            case POTATO_CACTUS:
                return "seedPotatoCactus";
            case SNAPDRAGON:
                return "seedSnapdragon";
            case SNAPE_GRASS:
                return "seedSnapeGrass";
            case TOADFLAX:
                return "seedToadflax";
            case TORSTOL:
                return "seedTorstol";
            default:
                throw new IllegalArgumentException("Unknown seed");
        }
    }

    static Set<SalvageSeed> selected(LazySalvagingConfig config) {
        Set<SalvageSeed> result = EnumSet.noneOf(SalvageSeed.class);
        if (config.seedAvantoe()) result.add(SalvageSeed.AVANTOE);
        if (config.seedBelladonna()) result.add(SalvageSeed.BELLADONNA);
        if (config.seedCactus()) result.add(SalvageSeed.CACTUS);
        if (config.seedCadantine()) result.add(SalvageSeed.CADANTINE);
        if (config.seedDwarfWeed()) result.add(SalvageSeed.DWARF_WEED);
        if (config.seedIrit()) result.add(SalvageSeed.IRIT);
        if (config.seedKwuarm()) result.add(SalvageSeed.KWUARM);
        if (config.seedLantadyme()) result.add(SalvageSeed.LANTADYME);
        if (config.seedPoisonIvy()) result.add(SalvageSeed.POISON_IVY);
        if (config.seedPotatoCactus()) result.add(SalvageSeed.POTATO_CACTUS);
        if (config.seedSnapdragon()) result.add(SalvageSeed.SNAPDRAGON);
        if (config.seedSnapeGrass()) result.add(SalvageSeed.SNAPE_GRASS);
        if (config.seedToadflax()) result.add(SalvageSeed.TOADFLAX);
        if (config.seedTorstol()) result.add(SalvageSeed.TORSTOL);
        return result;
    }

    static void save(ConfigManager manager, Set<SalvageSeed> selected) {
        // Remove first, so replacing a type never briefly creates seven selections.
        for (SalvageSeed seed : SalvageSeed.values())
            if (!selected.contains(seed))
                manager.setConfiguration("lazysalvaging", key(seed), false);
        for (SalvageSeed seed : selected)
            manager.setConfiguration("lazysalvaging", key(seed), true);
    }
}
