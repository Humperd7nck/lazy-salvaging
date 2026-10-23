package com.lazysalvaging;

import com.google.inject.Provides;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Collections;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Lazy Salvaging",
        description = "Highlights salvaging tasks, loot and the crystal extractor",
        tags = {"sailing", "salvaging"},
        enabledByDefault = false)
public class LazySalvagingPlugin extends Plugin {
    public static final String VERSION = "1.0.0";
    @Inject private GuideController guide;
    @Inject private LazySalvagingConfig config;
    @Inject private ConfigManager configManager;
    @Inject private GuideOverlay guideOverlay;
    @Inject private GuideHud guideHud;
    @Inject private OverlayManager overlayManager;
    @Inject private EventBus eventBus;

    @Provides
    LazySalvagingConfig provideConfig(ConfigManager manager) {
        return manager.getConfig(LazySalvagingConfig.class);
    }

    @Override
    protected void startUp() {
        // Preserve older seed selections in the regular settings checkboxes.
        SeedSettings.save(configManager, SeedSettings.selected(config));
        configManager.setConfiguration("lazysalvaging", "selectedSeeds", Collections.emptySet());
        guide.startSession();
        eventBus.register(guide);
        overlayManager.add(guideOverlay);
        overlayManager.add(guideHud);
    }

    @Override
    protected void shutDown() {
        eventBus.unregister(guide);
        overlayManager.remove(guideOverlay);
        overlayManager.remove(guideHud);
    }
}
