package com.lazysalvaging;

import net.runelite.client.config.ConfigItem;

import java.util.*;

public final class SeedSettingsTest {
    public static void main(String[] args) throws Exception {
        Set<SalvageSeed> previous =
                EnumSet.of(SalvageSeed.IRIT, SalvageSeed.TOADFLAX, SalvageSeed.AVANTOE);
        LazySalvagingConfig legacy =
                new LazySalvagingConfig() {
                    @Override
                    public Set<SalvageSeed> selectedSeeds() {
                        return previous;
                    }
                };
        if (!SeedSettings.selected(legacy).equals(previous))
            throw new AssertionError("existing choices must migrate");
        LazySalvagingConfig changed =
                new LazySalvagingConfig() {
                    @Override
                    public Set<SalvageSeed> selectedSeeds() {
                        return previous;
                    }

                    @Override
                    public boolean seedIrit() {
                        return false;
                    }

                    @Override
                    public boolean seedTorstol() {
                        return true;
                    }
                };
        if (!SeedSettings.selected(changed)
                .equals(EnumSet.of(SalvageSeed.TOADFLAX, SalvageSeed.AVANTOE, SalvageSeed.TORSTOL)))
            throw new AssertionError("independent checkbox overrides preserve other choices");
        if (!SeedSettings.selected(new LazySalvagingConfig() {}).isEmpty())
            throw new AssertionError("empty default");
        for (SalvageSeed seed : SalvageSeed.values()) {
            java.lang.reflect.Method method =
                    LazySalvagingConfig.class.getMethod(SeedSettings.key(seed));
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (method.getReturnType() != boolean.class
                    || item.hidden()
                    || !item.section().equals(LazySalvagingConfig.seedBoxSection)
                    || !item.keyName().equals(SeedSettings.key(seed)))
                throw new AssertionError("visible native checkbox for " + seed);
        }
        if (!LazySalvagingConfig.class
                .getMethod("selectedSeeds")
                .getAnnotation(ConfigItem.class)
                .hidden()) throw new AssertionError("old list hidden");
        System.out.println(
                "Seed settings: migration, independent checkboxes, defaults, all 14 controls"
                    + " passed.");
    }
}
