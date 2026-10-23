package com.lazysalvaging;

import java.util.Locale;

/** Only explicitly listed loot can become an alchemy or drop target. */
final class LootPolicy {
    static GuideEngine.Kind kind(String name, LazySalvagingConfig c) {
        for (SalvageSeed seed : SeedSettings.selected(c))
            if (seed.itemName.equalsIgnoreCase(name)) return GuideEngine.Kind.SELECTED_SEED;
        switch (name.toLowerCase(Locale.ROOT).replace('’', '\'')) {
            case "sailors' amulet (inert)":
                return classify(c.lootSailorsAmulet().name(), GuideEngine.Kind.OTHER);
            case "facility bottle (empty)":
                return classify(c.lootFacilityBottle().name(), GuideEngine.Kind.OTHER);
            case "mouldy doll":
                return classify(c.lootMouldyDoll().name(), GuideEngine.Kind.OTHER);
            case "dragon nails":
                return classify(c.lootDragonNails().name(), GuideEngine.Kind.OTHER);
            case "dragon cannon barrel":
                return classify(c.lootDragonCannonBarrel().name(), GuideEngine.Kind.OTHER);
            case "rune cannonball":
                return classify(c.lootRuneCannonball().name(), GuideEngine.Kind.OTHER);
            case "dragon cannonball":
                return classify(c.lootDragonCannonball().name(), GuideEngine.Kind.OTHER);
            case "fish offcuts":
                return classify(c.lootFishOffcuts().name(), GuideEngine.Kind.OTHER);
            case "ironwood repair kit":
                return classify(c.lootIronwoodRepairKit().name(), GuideEngine.Kind.OTHER);
            case "silver ore":
                return classify(c.lootSilverOre().name(), GuideEngine.Kind.OTHER);
            case "silver bar":
                return classify(c.lootSilverBar().name(), GuideEngine.Kind.OTHER);
            case "tiara":
                return classify(c.lootTiara().name(), GuideEngine.Kind.OTHER);
            case "jug of wine":
                return classify(c.lootJugOfWine().name(), GuideEngine.Kind.OTHER);
            case "spice":
                return classify(c.lootSpice().name(), GuideEngine.Kind.OTHER);
            case "grey wolf fur":
                return classify(c.lootGreyWolfFur().name(), GuideEngine.Kind.OTHER);
            case "silk":
                return classify(c.lootSilk().name(), GuideEngine.Kind.OTHER);
            case "nature talisman":
                return classify(c.lootNatureTalisman().name(), GuideEngine.Kind.OTHER);
            case "rune javelin":
                return classify(c.lootRuneJavelin().name(), GuideEngine.Kind.OTHER);
            case "loop half of key":
                return classify(c.lootLoopHalfKey().name(), GuideEngine.Kind.OTHER);
            case "tooth half of key":
                return classify(c.lootToothHalfKey().name(), GuideEngine.Kind.OTHER);
            case "rune spear":
                return classify(c.lootRuneSpear().name(), GuideEngine.Kind.OTHER);
            case "shield left half":
                return classify(c.lootShieldLeftHalf().name(), GuideEngine.Kind.OTHER);
            case "dragon spear":
                return classify(c.lootDragonSpear().name(), GuideEngine.Kind.OTHER);
            case "belladonna seed":
                return classify(c.lootBelladonnaSeed().name(), GuideEngine.Kind.OTHER);
            case "poison ivy seed":
                return classify(c.lootPoisonIvySeed().name(), GuideEngine.Kind.OTHER);
            case "cactus seed":
                return classify(c.lootCactusSeed().name(), GuideEngine.Kind.OTHER);
            case "potato cactus seed":
                return classify(c.lootPotatoCactusSeed().name(), GuideEngine.Kind.OTHER);
            case "snape grass seed":
                return classify(c.lootSnapeGrassSeed().name(), GuideEngine.Kind.OTHER);
            case "toadflax seed":
                return classify(c.lootToadflaxSeed().name(), GuideEngine.Kind.OTHER);
            case "irit seed":
                return classify(c.lootIritSeed().name(), GuideEngine.Kind.OTHER);
            case "avantoe seed":
                return classify(c.lootAvantoeSeed().name(), GuideEngine.Kind.OTHER);
            case "kwuarm seed":
                return classify(c.lootKwuarmSeed().name(), GuideEngine.Kind.OTHER);
            case "snapdragon seed":
                return classify(c.lootSnapdragonSeed().name(), GuideEngine.Kind.OTHER);
            case "cadantine seed":
                return classify(c.lootCadantineSeed().name(), GuideEngine.Kind.OTHER);
            case "lantadyme seed":
                return classify(c.lootLantadymeSeed().name(), GuideEngine.Kind.OTHER);
            case "dwarf weed seed":
                return classify(c.lootDwarfWeedSeed().name(), GuideEngine.Kind.OTHER);
            case "torstol seed":
                return classify(c.lootTorstolSeed().name(), GuideEngine.Kind.OTHER);
            case "grimy avantoe":
                return classify(c.lootGrimyAvantoe().name(), GuideEngine.Kind.HERB);
            case "grimy ranarr weed":
                return classify(c.lootGrimyRanarrWeed().name(), GuideEngine.Kind.HERB);
            case "grimy snapdragon":
                return classify(c.lootGrimySnapdragon().name(), GuideEngine.Kind.HERB);
            case "grimy torstol":
                return classify(c.lootGrimyTorstol().name(), GuideEngine.Kind.HERB);
            case "grimy kwuarm":
                return classify(c.lootGrimyKwuarm().name(), GuideEngine.Kind.HERB);
            case "grimy dwarf weed":
                return classify(c.lootGrimyDwarfWeed().name(), GuideEngine.Kind.HERB);
            case "grimy cadantine":
                return classify(c.lootGrimyCadantine().name(), GuideEngine.Kind.HERB);
            case "grimy lantadyme":
                return classify(c.lootGrimyLantadyme().name(), GuideEngine.Kind.HERB);
            case "uncut opal":
                return classify(c.lootUncutOpal().name(), GuideEngine.Kind.GEM);
            case "uncut jade":
                return classify(c.lootUncutJade().name(), GuideEngine.Kind.GEM);
            case "uncut red topaz":
                return classify(c.lootUncutRedTopaz().name(), GuideEngine.Kind.GEM);
            case "uncut sapphire":
                return classify(c.lootUncutSapphire().name(), GuideEngine.Kind.GEM);
            case "uncut emerald":
                return classify(c.lootUncutEmerald().name(), GuideEngine.Kind.GEM);
            case "uncut ruby":
                return classify(c.lootUncutRuby().name(), GuideEngine.Kind.GEM);
            case "uncut diamond":
                return classify(c.lootUncutDiamond().name(), GuideEngine.Kind.GEM);
            default:
                return GuideEngine.Kind.OTHER;
        }
    }

    private static GuideEngine.Kind classify(String action, GuideEngine.Kind stored) {
        switch (action) {
            case "DROP":
                return GuideEngine.Kind.DROP;
            case "ALCH":
                return GuideEngine.Kind.ALCHEMY;
            case "STORE":
                return stored;
            default:
                return GuideEngine.Kind.OTHER;
        }
    }

    static boolean drop(GuideEngine.Entry item, GuideEngine.View v) {
        if (item.kind == GuideEngine.Kind.DROP) return true;
        if (!v.dropOverflow || item.fit != GuideEngine.Fit.NO) return false;
        return item.kind == GuideEngine.Kind.GEM && v.gemsPresent
                || item.kind == GuideEngine.Kind.HERB && v.herbsPresent
                || item.kind == GuideEngine.Kind.SELECTED_SEED && v.seedsPresent;
    }
}
