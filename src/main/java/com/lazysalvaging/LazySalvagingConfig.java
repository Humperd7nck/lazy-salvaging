package com.lazysalvaging;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("lazysalvaging")
public interface LazySalvagingConfig extends Config {
    @ConfigSection(
            name = "Seed Box",
            description = "Choose up to 6 seeds you want to place in the seed box.",
            position = 20,
            closedByDefault = true)
    String seedBoxSection = "seedBox";

    @ConfigItem(
            keyName = "selectedSeeds",
            name = "Previous seed selection",
            description = "Compatibility with earlier builds",
            hidden = true)
    default java.util.Set<SalvageSeed> selectedSeeds() {
        return java.util.Collections.emptySet();
    }

    @ConfigItem(
            keyName = "seedAvantoe",
            name = "Avantoe seed",
            description = "Store avantoe seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 0)
    default boolean seedAvantoe() {
        return selectedSeeds().contains(SalvageSeed.AVANTOE);
    }

    @ConfigItem(
            keyName = "seedBelladonna",
            name = "Belladonna seed",
            description = "Store belladonna seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 1)
    default boolean seedBelladonna() {
        return selectedSeeds().contains(SalvageSeed.BELLADONNA);
    }

    @ConfigItem(
            keyName = "seedCactus",
            name = "Cactus seed",
            description = "Store cactus seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 2)
    default boolean seedCactus() {
        return selectedSeeds().contains(SalvageSeed.CACTUS);
    }

    @ConfigItem(
            keyName = "seedCadantine",
            name = "Cadantine seed",
            description = "Store cadantine seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 3)
    default boolean seedCadantine() {
        return selectedSeeds().contains(SalvageSeed.CADANTINE);
    }

    @ConfigItem(
            keyName = "seedDwarfWeed",
            name = "Dwarf weed seed",
            description = "Store dwarf weed seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 4)
    default boolean seedDwarfWeed() {
        return selectedSeeds().contains(SalvageSeed.DWARF_WEED);
    }

    @ConfigItem(
            keyName = "seedIrit",
            name = "Irit seed",
            description = "Store irit seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 5)
    default boolean seedIrit() {
        return selectedSeeds().contains(SalvageSeed.IRIT);
    }

    @ConfigItem(
            keyName = "seedKwuarm",
            name = "Kwuarm seed",
            description = "Store kwuarm seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 6)
    default boolean seedKwuarm() {
        return selectedSeeds().contains(SalvageSeed.KWUARM);
    }

    @ConfigItem(
            keyName = "seedLantadyme",
            name = "Lantadyme seed",
            description = "Store lantadyme seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 7)
    default boolean seedLantadyme() {
        return selectedSeeds().contains(SalvageSeed.LANTADYME);
    }

    @ConfigItem(
            keyName = "seedPoisonIvy",
            name = "Poison ivy seed",
            description = "Store poison ivy seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 8)
    default boolean seedPoisonIvy() {
        return selectedSeeds().contains(SalvageSeed.POISON_IVY);
    }

    @ConfigItem(
            keyName = "seedPotatoCactus",
            name = "Potato cactus seed",
            description = "Store potato cactus seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 9)
    default boolean seedPotatoCactus() {
        return selectedSeeds().contains(SalvageSeed.POTATO_CACTUS);
    }

    @ConfigItem(
            keyName = "seedSnapdragon",
            name = "Snapdragon seed",
            description = "Store snapdragon seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 10)
    default boolean seedSnapdragon() {
        return selectedSeeds().contains(SalvageSeed.SNAPDRAGON);
    }

    @ConfigItem(
            keyName = "seedSnapeGrass",
            name = "Snape grass seed",
            description = "Store snape grass seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 11)
    default boolean seedSnapeGrass() {
        return selectedSeeds().contains(SalvageSeed.SNAPE_GRASS);
    }

    @ConfigItem(
            keyName = "seedToadflax",
            name = "Toadflax seed",
            description = "Store toadflax seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 12)
    default boolean seedToadflax() {
        return selectedSeeds().contains(SalvageSeed.TOADFLAX);
    }

    @ConfigItem(
            keyName = "seedTorstol",
            name = "Torstol seed",
            description = "Store torstol seed in the seed box. Choose up to six seed types.",
            section = seedBoxSection,
            position = 13)
    default boolean seedTorstol() {
        return selectedSeeds().contains(SalvageSeed.TORSTOL);
    }

    @ConfigItem(
            keyName = "preview",
            name = "Preview next",
            description = "Show an orange preview when the next target is known and visible",
            position = 0)
    default boolean preview() {
        return true;
    }

    @ConfigItem(
            keyName = "cargoSwap",
            name = "Cargo left-click swap",
            description =
                    "Use Deposit-all in Active; Open in Sorting or when a fresh cargo check is"
                        + " needed",
            position = 1,
            hidden = true)
    default boolean cargoSwap() {
        return true;
    }

    @ConfigItem(
            keyName = "current",
            name = "Current task",
            description = "World objects, tabs, spells and bag actions",
            position = 2)
    default Color current() {
        return Color.CYAN;
    }

    @ConfigItem(
            keyName = "next",
            name = "Preview task",
            description = "Next hover target",
            position = 3)
    default Color next() {
        return Color.ORANGE;
    }

    @ConfigItem(
            keyName = "alchemy",
            name = "Alch color",
            description = "Current inventory item to alchemize",
            section = lootSection,
            position = 1)
    default Color alchemy() {
        return Color.MAGENTA;
    }

    @ConfigItem(
            keyName = "drop",
            name = "Drop color",
            description = "All eligible cleanup drops",
            section = lootSection,
            position = 0)
    default Color drop() {
        return Color.RED;
    }

    @Range(min = 0, max = 100)
    @ConfigItem(
            keyName = "fill",
            name = "Fill opacity",
            description = "Highlight fill percentage",
            position = 6)
    default int fill() {
        return 18;
    }

    @ConfigItem(
            keyName = "hud",
            name = "Info panel",
            description = "Show active phase, cargo occupancy and session totals",
            position = 7)
    default boolean hud() {
        return true;
    }

    @ConfigItem(
            keyName = "preventBoatMisclicks",
            name = "Prevent boat misclicks",
            description =
                    "Make inactive boat objects and crew default to Walk here. Normal actions"
                        + " remain on right-click. Walking can interrupt your activity.",
            position = 8,
            hidden = true)
    default boolean preventBoatMisclicks() {
        return false;
    }

    @ConfigItem(
            keyName = "trackExtractor",
            name = "Crystal extractor",
            description =
                    "Activate or harvest the extractor after the current salvaging, sorting or"
                        + " cleanup step finishes",
            position = 9)
    default boolean trackExtractor() {
        return false;
    }

    @ConfigItem(
            keyName = "dropSwap",
            name = "Left-click cleanup drops",
            description =
                    "Default eligible red inventory items to Drop only during the cleanup drop"
                        + " step",
            position = 10,
            hidden = true)
    default boolean dropSwap() {
        return true;
    }

    @ConfigSection(
            name = "Tab highlight colors",
            description =
                    "Separate current-task colors for Inventory, Magic, Sailing Options and the"
                        + " cargo hold close button.",
            position = 21,
            closedByDefault = true)
    String hotkeyColorsSection = "hotkeyColors";

    @ConfigItem(
            keyName = "inventoryTabColor",
            name = "Inventory tab",
            description =
                    "Current Inventory-tab highlight; use your Inventory hotkey if preferred. Hover"
                        + " previews keep the preview color.",
            section = hotkeyColorsSection,
            position = 0)
    default Color inventoryTabColor() {
        return current();
    }

    @ConfigItem(
            keyName = "magicTabColor",
            name = "Magic spellbook tab",
            description =
                    "Current Magic-tab highlight; use your Magic hotkey if preferred. Does not"
                        + " recolor High Level Alchemy or hover previews.",
            section = hotkeyColorsSection,
            position = 1)
    default Color magicTabColor() {
        return current();
    }

    @ConfigItem(
            keyName = "cargoCloseColor",
            name = "Cargo hold X",
            description =
                    "Current cargo-hold close-button highlight; a reminder to use Escape if enabled"
                        + " in your game settings. Hover previews keep the preview color.",
            section = hotkeyColorsSection,
            position = 2)
    default Color cargoCloseColor() {
        return current();
    }

    @ConfigItem(
            keyName = "sailingTabColor",
            name = "Sailing Options tab",
            description =
                    "Current Sailing Options-tab highlight. Hover previews keep the preview color.",
            section = hotkeyColorsSection,
            position = 3)
    default Color sailingTabColor() {
        return current();
    }

    @ConfigItem(
            keyName = "menuSwap",
            name = "Menu swap: left clicks only",
            description =
                    "Enable all menu changes: cargo actions, cleanup Drop, bag Check, inactive boat"
                        + " targets and shipwreck Inspect to Walk here. Right-click options remain"
                        + " available.",
            position = 1)
    default boolean menuSwap() {
        return cargoSwap() || dropSwap() || preventBoatMisclicks();
    }

    @Range(min = 0, max = 100)
    @ConfigItem(
            keyName = "hudOpacity",
            name = "Info panel opacity",
            description = "Background opacity percentage. Text stays readable.",
            position = 8)
    default int hudOpacity() {
        return 70;
    }

    @ConfigSection(
            name = "Loot categories",
            description =
                    "Select Drop/Keep/Alch for each item to make inventory management lazy. Store"
                        + " sends eligible items to their container.",
            position = 19,
            closedByDefault = true)
    String lootSection = "loot";

    @ConfigItem(
            keyName = "dropOverflow",
            name = "Drop container overflow",
            description =
                    "When enabled, highlights \"Stored\" items as drop when their bag is full.",
            section = lootSection,
            position = 2)
    default boolean dropOverflow() {
        return false;
    }

    @ConfigItem(
            keyName = "GenerallootHeader",
            name = "<html><b>General loot</b></html>",
            description = "Choose how to handle general loot.",
            section = lootSection,
            position = 10)
    default void generallootHeader() {}

    @ConfigItem(
            keyName = "lootSailorsAmulet",
            name = "Sailors’ amulet (inert)",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 11)
    default LootAction lootSailorsAmulet() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootFacilityBottle",
            name = "Facility bottle (empty)",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 12)
    default LootAction lootFacilityBottle() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootMouldyDoll",
            name = "Mouldy doll",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 13)
    default LootAction lootMouldyDoll() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootDragonNails",
            name = "Dragon nails",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 14)
    default LootAction lootDragonNails() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootDragonCannonBarrel",
            name = "Dragon cannon barrel",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 15)
    default LootAction lootDragonCannonBarrel() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootRuneCannonball",
            name = "Rune cannonball",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 16)
    default LootAction lootRuneCannonball() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootDragonCannonball",
            name = "Dragon cannonball",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 17)
    default LootAction lootDragonCannonball() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootFishOffcuts",
            name = "Fish offcuts",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 18)
    default LootAction lootFishOffcuts() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootIronwoodRepairKit",
            name = "Ironwood repair kit",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 19)
    default LootAction lootIronwoodRepairKit() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootSilverOre",
            name = "Silver ore",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 20)
    default LootAction lootSilverOre() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootSilverBar",
            name = "Silver bar",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 21)
    default LootAction lootSilverBar() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootTiara",
            name = "Tiara",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 22)
    default LootAction lootTiara() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootJugOfWine",
            name = "Jug of wine",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 23)
    default LootAction lootJugOfWine() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootSpice",
            name = "Spice",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 24)
    default LootAction lootSpice() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootGreyWolfFur",
            name = "Grey wolf fur",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 25)
    default LootAction lootGreyWolfFur() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootSilk",
            name = "Silk",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 26)
    default LootAction lootSilk() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootNatureTalisman",
            name = "Nature talisman",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 27)
    default LootAction lootNatureTalisman() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootRuneJavelin",
            name = "Rune javelin",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 28)
    default LootAction lootRuneJavelin() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootLoopHalfKey",
            name = "Loop half of key",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 29)
    default LootAction lootLoopHalfKey() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootToothHalfKey",
            name = "Tooth half of key",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 30)
    default LootAction lootToothHalfKey() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootRuneSpear",
            name = "Rune spear",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 31)
    default LootAction lootRuneSpear() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootShieldLeftHalf",
            name = "Shield left half",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 32)
    default LootAction lootShieldLeftHalf() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "lootDragonSpear",
            name = "Dragon spear",
            description = "Keep leaves this item alone. Drop and Alch guide cleanup.",
            section = lootSection,
            position = 33)
    default LootAction lootDragonSpear() {
        return LootAction.ALCH;
    }

    @ConfigItem(
            keyName = "SeedsHeader",
            name = "<html><b>Seeds</b></html>",
            description =
                    "Choose up to six seed types in Seed Box. Those selections take storage"
                        + " priority over these dropdowns.",
            section = lootSection,
            position = 34)
    default void seedsHeader() {}

    @ConfigItem(
            keyName = "lootBelladonnaSeed",
            name = "Belladonna seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 35)
    default LootAction lootBelladonnaSeed() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootPoisonIvySeed",
            name = "Poison ivy seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 36)
    default LootAction lootPoisonIvySeed() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootCactusSeed",
            name = "Cactus seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 37)
    default LootAction lootCactusSeed() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootPotatoCactusSeed",
            name = "Potato cactus seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 38)
    default LootAction lootPotatoCactusSeed() {
        return LootAction.DROP;
    }

    @ConfigItem(
            keyName = "lootSnapeGrassSeed",
            name = "Snape grass seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 39)
    default LootAction lootSnapeGrassSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootToadflaxSeed",
            name = "Toadflax seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 40)
    default LootAction lootToadflaxSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootIritSeed",
            name = "Irit seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 41)
    default LootAction lootIritSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootAvantoeSeed",
            name = "Avantoe seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 42)
    default LootAction lootAvantoeSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootKwuarmSeed",
            name = "Kwuarm seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 43)
    default LootAction lootKwuarmSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootSnapdragonSeed",
            name = "Snapdragon seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 44)
    default LootAction lootSnapdragonSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootCadantineSeed",
            name = "Cadantine seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 45)
    default LootAction lootCadantineSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootLantadymeSeed",
            name = "Lantadyme seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 46)
    default LootAction lootLantadymeSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootDwarfWeedSeed",
            name = "Dwarf weed seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 47)
    default LootAction lootDwarfWeedSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "lootTorstolSeed",
            name = "Torstol seed",
            description =
                    "Selected Seed Box types are stored first; the overflow checkbox controls"
                        + " confirmed full-box leftovers.",
            section = lootSection,
            position = 48)
    default LootAction lootTorstolSeed() {
        return LootAction.KEEP;
    }

    @ConfigItem(
            keyName = "HerbsHeader",
            name = "<html><b>Herbs</b></html>",
            description = "Choose how to handle herbs.",
            section = lootSection,
            position = 49)
    default void herbsHeader() {}

    @ConfigItem(
            keyName = "lootGrimyAvantoe",
            name = "Grimy avantoe",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 50)
    default StoredLootAction lootGrimyAvantoe() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyRanarrWeed",
            name = "Grimy ranarr weed",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 51)
    default StoredLootAction lootGrimyRanarrWeed() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimySnapdragon",
            name = "Grimy snapdragon",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 52)
    default StoredLootAction lootGrimySnapdragon() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyTorstol",
            name = "Grimy torstol",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 53)
    default StoredLootAction lootGrimyTorstol() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyKwuarm",
            name = "Grimy kwuarm",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 54)
    default StoredLootAction lootGrimyKwuarm() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyDwarfWeed",
            name = "Grimy dwarf weed",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 55)
    default StoredLootAction lootGrimyDwarfWeed() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyCadantine",
            name = "Grimy cadantine",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 56)
    default StoredLootAction lootGrimyCadantine() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootGrimyLantadyme",
            name = "Grimy lantadyme",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 57)
    default StoredLootAction lootGrimyLantadyme() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "GemsHeader",
            name = "<html><b>Gems</b></html>",
            description = "Choose how to handle gems.",
            section = lootSection,
            position = 58)
    default void gemsHeader() {}

    @ConfigItem(
            keyName = "lootUncutOpal",
            name = "Uncut opal",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 59)
    default StoredLootAction lootUncutOpal() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutJade",
            name = "Uncut jade",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 60)
    default StoredLootAction lootUncutJade() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutRedTopaz",
            name = "Uncut red topaz",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 61)
    default StoredLootAction lootUncutRedTopaz() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutSapphire",
            name = "Uncut sapphire",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 62)
    default StoredLootAction lootUncutSapphire() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutEmerald",
            name = "Uncut emerald",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 63)
    default StoredLootAction lootUncutEmerald() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutRuby",
            name = "Uncut ruby",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 64)
    default StoredLootAction lootUncutRuby() {
        return StoredLootAction.STORE;
    }

    @ConfigItem(
            keyName = "lootUncutDiamond",
            name = "Uncut diamond",
            description =
                    "Keep leaves this item alone. Drop and Alch guide cleanup. Store uses its"
                        + " container; overflow follows the overflow checkbox.",
            section = lootSection,
            position = 65)
    default StoredLootAction lootUncutDiamond() {
        return StoredLootAction.STORE;
    }
}
