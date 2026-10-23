package com.lazysalvaging;

import net.runelite.api.*;

import java.lang.reflect.Proxy;

public final class DropMenuSwapTest {
    static int checks;

    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks++;
    }

    static MenuEntry entry(String option, int widget, int slot, int id) {
        return entry(option, widget, slot, id, MenuAction.CC_OP);
    }

    static MenuEntry entry(String option, int widget, int slot, int id, MenuAction type) {
        return (MenuEntry)
                Proxy.newProxyInstance(
                        MenuEntry.class.getClassLoader(),
                        new Class<?>[] {MenuEntry.class},
                        (p, m, a) -> {
                            switch (m.getName()) {
                                case "getType":
                                    return type;
                                case "getOption":
                                    return option;
                                case "getParam1":
                                    return widget;
                                case "getParam0":
                                    return slot;
                                case "getItemId":
                                    return id;
                                default:
                                    throw new AssertionError(m.getName());
                            }
                        });
    }

    public static void main(String[] args) {
        int inv = 149 << 16;
        MenuEntry drop = entry("Drop", inv, 3, 100), use = entry("Use", inv, 3, 100);
        check(
                DropMenuSwap.select(new MenuEntry[] {drop, use}, 3, 100, true) == 0,
                "promote existing Drop");
        check(
                DropMenuSwap.select(new MenuEntry[] {drop, use}, 3, 100, false) == -1,
                "not eligible untouched");
        check(
                DropMenuSwap.select(new MenuEntry[] {drop, use}, 3, 101, true) == -1,
                "stale item mismatch untouched");
        check(
                DropMenuSwap.select(new MenuEntry[] {drop, use}, 4, 100, true) == -1,
                "other slot untouched");
        check(DropMenuSwap.select(new MenuEntry[] {use}, 3, 100, true) == -1, "no invented Drop");
        check(
                DropMenuSwap.select(
                                new MenuEntry[] {entry("Drop", 944 << 16, 3, 100)}, 3, 100, true)
                        == -1,
                "cargo inventory untouched");
        check(
                DropMenuSwap.select(
                                new MenuEntry[] {entry("Destroy", inv, 3, 100), use}, 3, 100, true)
                        == -1,
                "Destroy never substituted");
        check(
                DropMenuSwap.eligible(GuideEngine.Kind.DROP, GuideEngine.Fit.UNKNOWN),
                "drop whitelist eligible");
        check(
                DropMenuSwap.eligible(GuideEngine.Kind.GEM, GuideEngine.Fit.NO),
                "confirmed gem overflow eligible");
        check(
                DropMenuSwap.eligible(GuideEngine.Kind.HERB, GuideEngine.Fit.NO),
                "confirmed herb overflow eligible");
        check(
                DropMenuSwap.eligible(GuideEngine.Kind.SEED, GuideEngine.Fit.NO),
                "confirmed seed overflow eligible");
        check(
                !DropMenuSwap.eligible(GuideEngine.Kind.GEM, GuideEngine.Fit.UNKNOWN),
                "unknown capacity protected");
        check(
                !DropMenuSwap.eligible(GuideEngine.Kind.HERB, GuideEngine.Fit.YES),
                "fitting herb protected");
        check(
                !DropMenuSwap.eligible(GuideEngine.Kind.ALCHEMY, GuideEngine.Fit.NO),
                "alchemy whitelist protected");
        check(
                !DropMenuSwap.eligible(GuideEngine.Kind.OTHER, GuideEngine.Fit.NO),
                "unlisted item protected");
        MenuAction[] priority = {MenuAction.CC_OP_LOW_PRIORITY};
        boolean[] deprioritized = {true};
        MenuEntry low =
                (MenuEntry)
                        Proxy.newProxyInstance(
                                MenuEntry.class.getClassLoader(),
                                new Class<?>[] {MenuEntry.class},
                                (proxy, method, arguments) -> {
                                    switch (method.getName()) {
                                        case "getType":
                                            return priority[0];
                                        case "setType":
                                            priority[0] = (MenuAction) arguments[0];
                                            return proxy;
                                        case "setDeprioritized":
                                            deprioritized[0] = (boolean) arguments[0];
                                            return proxy;
                                        default:
                                            throw new AssertionError(
                                                    "Unexpected target mutation: "
                                                            + method.getName());
                                    }
                                });
        MenuEntry[] menu = {low, use};
        DropMenuSwap.promote(menu, 0);
        check(priority[0] == MenuAction.CC_OP, "right-click-only opcode normalized");
        check(!deprioritized[0], "deprioritized flag removed");
        check(menu[1] == low && menu[0] == use, "Drop promoted and other action preserved");
        DropMenuSwap.promote(menu, 1);
        check(priority[0] == MenuAction.CC_OP, "normal priority remains normal");
        MenuEntry checkEntry = entry("Check", inv, 2, 33395), fill = entry("Fill", inv, 2, 33395);
        check(
                DropMenuSwap.selectOption(
                                new MenuEntry[] {checkEntry, fill}, 2, 33395, true, "Check")
                        == 0,
                "existing bag Check promoted");
        check(
                DropMenuSwap.selectOption(
                                new MenuEntry[] {checkEntry, fill}, 2, 33395, false, "Check")
                        == -1,
                "non-current bag unchanged");
        check(
                DropMenuSwap.selectOption(
                                new MenuEntry[] {checkEntry, fill}, 2, 33137, true, "Check")
                        == -1,
                "bag identity mismatch rejected");
        check(
                DropMenuSwap.selectOption(new MenuEntry[] {fill}, 2, 33395, true, "Check") == -1,
                "missing Check not invented");
        MenuEntry select = entry("Use", inv, 3, 100, MenuAction.WIDGET_TARGET);
        check(
                DropMenuSwap.selectUse(new MenuEntry[] {select, drop}, 3, 100) == 0,
                "existing Use promoted for individual storage");
        check(
                DropMenuSwap.selectUse(new MenuEntry[] {select, drop}, 4, 100) == -1,
                "Use stays scoped to current slot");
        check(
                DropMenuSwap.selectUse(new MenuEntry[] {select, drop}, 3, 101) == -1,
                "Use rejects stale item");
        check(
                DropMenuSwap.selectUse(new MenuEntry[] {use, drop}, 3, 100) == -1,
                "Use requires item-selection opcode");
        System.out.println("Drop menu swap: " + checks + " checks passed.");
    }
}
