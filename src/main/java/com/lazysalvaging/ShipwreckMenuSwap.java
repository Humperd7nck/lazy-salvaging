package com.lazysalvaging;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

final class ShipwreckMenuSwap {
    static int select(MenuEntry[] entries) {
        if (entries.length == 0) return -1;
        MenuEntry top = entries[entries.length - 1];
        String target =
                top.getTarget() == null ? "" : top.getTarget().replaceAll("<[^>]*>", "").trim();
        if (!"Inspect".equals(top.getOption()) || !"Merchant shipwreck".equalsIgnoreCase(target))
            return -1;
        boolean worldAction = BoatMenuGuard.boatAction(top, top.getWorldViewId());
        switch (top.getType()) {
            case WORLD_ENTITY_FIRST_OPTION:
            case WORLD_ENTITY_SECOND_OPTION:
            case WORLD_ENTITY_THIRD_OPTION:
            case WORLD_ENTITY_FOURTH_OPTION:
            case WORLD_ENTITY_FIFTH_OPTION:
                worldAction = true;
                break;
            default:
                break;
        }
        if (!worldAction) return -1;
        for (int i = entries.length - 1; i >= 0; i--)
            if (entries[i].getType() == MenuAction.WALK) return i;
        return -1;
    }
}
