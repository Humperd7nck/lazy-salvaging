package com.lazysalvaging;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class LazySalvagingLauncher {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(LazySalvagingPlugin.class);
        RuneLite.main(args);
    }
}
