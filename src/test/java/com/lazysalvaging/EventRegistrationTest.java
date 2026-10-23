package com.lazysalvaging;

import net.runelite.client.eventbus.EventBus;

/** Exercise RuneLite's actual registration validation, not just Java compilation. */
public final class EventRegistrationTest {
    public static void main(String[] args) {
        EventBus bus = new EventBus();
        LazySalvagingPlugin plugin = new LazySalvagingPlugin();
        GuideController controller = new GuideController();
        bus.register(plugin);
        bus.register(controller);
        bus.unregister(controller);
        bus.unregister(plugin);
        System.out.println("RuneLite event registration: plugin and controller passed.");
    }
}
