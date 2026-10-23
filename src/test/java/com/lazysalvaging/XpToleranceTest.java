package com.lazysalvaging;

public final class XpToleranceTest {
    private static int checks;

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }

    public static void main(String[] args) {
        int[][] normal = {{97, 98, 205}, {82, 179, 180, 287}, {164, 261, 262, 369}};
        for (int crew = 0; crew < 3; crew++)
            for (int base : normal[crew])
                for (int offset = -1; offset <= 1; offset++)
                    check(
                            CargoCounter.crewForXp(base + offset) == crew,
                            "normal tolerance " + base + "/" + offset);
        for (int crew = 0; crew < 3; crew++)
            for (int base : new int[] {250, 256, 257})
                for (int offset = -1; offset <= 1; offset++) {
                    int delta = base + 82 * crew + offset;
                    check(
                            CargoCounter.extractorCrewForXp(delta) == crew,
                            "extractor tolerance " + delta);
                    CargoCounter counter = new CargoCounter();
                    counter.reset(1000);
                    counter.observe(100);
                    counter.xp(1000 + delta, true, true);
                    check(
                            counter.known() && counter.slots() == 100 + crew,
                            "only crew changes cargo " + delta);
                }
        for (int delta = 1; delta < 600; delta++) {
            int normalCrew = CargoCounter.crewForXp(delta),
                    extractorCrew = CargoCounter.extractorCrewForXp(delta);
            check(
                    normalCrew < 0 || extractorCrew < 0,
                    "normal and extractor ranges do not overlap at " + delta);
        }
        for (int delta :
                new int[] {
                    80, 84, 95, 100, 166, 203, 207, 248, 252, 254, 259, 264, 285, 289, 367, 371
                })
            check(
                    CargoCounter.crewForXp(delta) < 0 && CargoCounter.extractorCrewForXp(delta) < 0,
                    "outside ranges remains unknown " + delta);
        CargoCounter counter = new CargoCounter();
        counter.reset(1000);
        counter.observe(100);
        counter.xp(1257, true, false);
        check(!counter.known(), "257 still requires harvest evidence");
        counter.reset(1000);
        counter.observe(100);
        counter.xp(1257, true, true);
        check(
                counter.known() && counter.slots() == 100,
                "screenshot 257 with harvest evidence preserves count");
        counter.xp(1257, true, true);
        check(counter.slots() == 100, "duplicate event not counted");
        System.out.println("XP tolerance: " + checks + " checks passed.");
    }
}
