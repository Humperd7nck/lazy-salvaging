package com.lazysalvaging;

import net.runelite.api.*;

import java.lang.reflect.Proxy;

public final class BoatMenuGuardTest {
    static int checks;

    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks++;
    }

    static MenuEntry entry(MenuAction type, int world) {
        return (MenuEntry)
                Proxy.newProxyInstance(
                        MenuEntry.class.getClassLoader(),
                        new Class<?>[] {MenuEntry.class},
                        (p, m, a) -> {
                            if (m.getName().equals("getType")) return type;
                            if (m.getName().equals("getWorldViewId")) return world;
                            if (m.getName().equals("equals")) return p == a[0];
                            if (m.getName().equals("hashCode")) return System.identityHashCode(p);
                            if (m.getName().equals("toString")) return type + "/" + world;
                            throw new AssertionError("Unexpected API use: " + m.getName());
                        });
    }

    public static void main(String[] args) {
        MenuEntry walk = entry(MenuAction.WALK, -1),
                hook = entry(MenuAction.GAME_OBJECT_FIRST_OPTION, 3),
                cargo = entry(MenuAction.GAME_OBJECT_SECOND_OPTION, 3),
                crew = entry(MenuAction.NPC_FIRST_OPTION, 3),
                otherBoat = entry(MenuAction.GAME_OBJECT_FIRST_OPTION, 4),
                widget = entry(MenuAction.CC_OP, 3),
                spell = entry(MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, 3),
                player = entry(MenuAction.PLAYER_FIRST_OPTION, 3);
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, cargo}, 3, -1) == 0,
                "inactive cargo becomes Walk");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, crew}, 3, -1) == 0,
                "inactive crew becomes Walk");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook, cargo, crew}, 3, 1) == 1,
                "current hook wins overlap");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook}, 3, -1) == 0,
                "preview or excluded hook inactive");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook}, 3, 1) == 1,
                "current hook stays usable");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, cargo, hook}, 3, 1) == 1,
                "current cargo promoted over hook");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, otherBoat}, 3, -1) == -1,
                "other boat untouched");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook, otherBoat}, 3, 1) == -1,
                "other boat top target untouched");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook, widget}, 3, 1) == -1,
                "widget untouched");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook, spell}, 3, 1) == -1,
                "spell target untouched");
        check(
                BoatMenuGuard.select(new MenuEntry[] {walk, hook, player}, 3, 1) == -1,
                "player untouched");
        check(
                BoatMenuGuard.select(new MenuEntry[] {hook}, 3, -1) == -1,
                "no fabricated Walk entry");
        check(
                BoatMenuGuard.select(new MenuEntry[] {hook, walk}, 3, 0) == 0,
                "current action can replace existing Walk priority");
        check(BoatMenuGuard.select(new MenuEntry[] {walk}, 3, -1) == -1, "ground alone untouched");
        check(BoatMenuGuard.select(new MenuEntry[] {}, 3, -1) == -1, "empty menu");
        MenuEntry[] entries = {walk, hook, cargo, crew};
        BoatMenuGuard.promote(entries, 0);
        check(entries[3] == walk, "Walk is default");
        check(
                entries[0] == hook && entries[1] == cargo && entries[2] == crew,
                "normal actions preserved in order for right click");
        BoatMenuGuard.promote(entries, 3);
        check(entries[3] == walk, "already top unchanged");
        System.out.println("Boat menu guard: " + checks + " checks passed.");
    }
}
