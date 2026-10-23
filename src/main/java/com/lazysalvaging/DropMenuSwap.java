package com.lazysalvaging;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

final class DropMenuSwap {
    static final int INVENTORY_WIDGET = 149 << 16;

    static void promote(MenuEntry[] entries, int index) {
        // CC_OP_LOW_PRIORITY is right-click-only even when placed on top.
        // Preserve the item operation identifier and target; change only its priority.
        if (entries[index].getType() == MenuAction.CC_OP_LOW_PRIORITY)
            entries[index].setType(MenuAction.CC_OP);
        entries[index].setDeprioritized(false);
        BoatMenuGuard.promote(entries, index);
    }

    static boolean eligible(GuideEngine.Kind kind, GuideEngine.Fit fit) {
        return kind == GuideEngine.Kind.DROP
                || (GuideEngine.isBag(kind) && fit == GuideEngine.Fit.NO);
    }

    static int select(MenuEntry[] entries, int slot, int itemId, boolean allowed) {
        return selectOption(entries, slot, itemId, allowed, "Drop");
    }

    static int selectUse(MenuEntry[] entries, int slot, int itemId) {
        if (entries.length == 0 || slot < 0 || itemId < 0) return -1;
        MenuEntry top = entries[entries.length - 1];
        if (top.getParam1() != INVENTORY_WIDGET
                || top.getParam0() != slot
                || top.getItemId() != itemId) return -1;
        for (int i = entries.length - 1; i >= 0; i--) {
            MenuEntry entry = entries[i];
            if (entry.getParam1() == INVENTORY_WIDGET
                    && entry.getParam0() == slot
                    && entry.getItemId() == itemId
                    && "Use".equals(entry.getOption())
                    && entry.getType() == MenuAction.WIDGET_TARGET) return i;
        }
        return -1;
    }

    static int selectOption(
            MenuEntry[] entries, int slot, int itemId, boolean allowed, String option) {
        if (!allowed || entries.length == 0 || slot < 0 || itemId < 0) return -1;
        MenuEntry top = entries[entries.length - 1];
        if (top.getParam1() != INVENTORY_WIDGET
                || top.getParam0() != slot
                || top.getItemId() != itemId) return -1;
        for (int i = entries.length - 1; i >= 0; i--) {
            MenuEntry entry = entries[i];
            if (entry.getParam1() == INVENTORY_WIDGET
                    && entry.getParam0() == slot
                    && entry.getItemId() == itemId
                    && option.equals(entry.getOption())
                    && (entry.getType() == MenuAction.CC_OP
                            || entry.getType() == MenuAction.CC_OP_LOW_PRIORITY)) return i;
        }
        return -1;
    }
}
