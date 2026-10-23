package com.lazysalvaging;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

/** Select only an existing action; never fabricate a Walk destination or input. */
final class BoatMenuGuard {
    static boolean objectAction(MenuAction type) {
        switch (type) {
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION:
                return true;
            default:
                return false;
        }
    }

    static boolean boatAction(MenuEntry entry, int worldViewId) {
        if (entry.getWorldViewId() != worldViewId) return false;
        if (objectAction(entry.getType())) return true;
        switch (entry.getType()) {
            case NPC_FIRST_OPTION:
            case NPC_SECOND_OPTION:
            case NPC_THIRD_OPTION:
            case NPC_FOURTH_OPTION:
            case NPC_FIFTH_OPTION:
                return true;
            default:
                return false;
        }
    }

    static int select(MenuEntry[] entries, int worldViewId, int currentIndex) {
        if (entries.length == 0) return -1;
        MenuEntry top = entries[entries.length - 1];
        if (!boatAction(top, worldViewId) && top.getType() != MenuAction.WALK) return -1;
        boolean boat = false;
        int walk = -1;
        for (int i = 0; i < entries.length; i++) {
            if (boatAction(entries[i], worldViewId)) boat = true;
            if (entries[i].getType() == MenuAction.WALK) walk = i;
        }
        if (!boat) return -1;
        if (currentIndex >= 0
                && currentIndex < entries.length
                && boatAction(entries[currentIndex], worldViewId)) return currentIndex;
        return walk;
    }

    static void promote(MenuEntry[] entries, int index) {
        MenuEntry chosen = entries[index];
        System.arraycopy(entries, index + 1, entries, index, entries.length - index - 1);
        entries[entries.length - 1] = chosen;
    }
}
