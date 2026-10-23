package com.lazysalvaging;

public final class SessionTotalsTest {
    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        SessionTotals s = new SessionTotals();
        s.reset(1000);
        int xp = 1000;
        for (int delta : new int[] {97, 98, 179, 180, 261, 262})
            s.xp(xp += delta, true, true, false);
        expect(
                s.sorted() == 6 && s.harvests() == 0,
                "combined rewards each count one sorted salvage");
        s.xp(xp, true, true, false);
        s.xp(xp += 82, true, true, false);
        s.xp(xp += 164, true, true, false);
        expect(s.sorted() == 6, "duplicate and crew-only XP ignored");
        s.xp(xp += 205, true, false, false);
        s.xp(xp += 97, false, true, false);
        s.xp(xp += 98, true, false, false);
        expect(s.sorted() == 6, "active salvage and unrelated XP ignored");
        for (int delta : new int[] {250, 256, 257, 332, 338, 339, 414, 420, 421})
            s.xp(xp += delta, true, false, true);
        expect(s.harvests() == 9, "harvest rewards count once with zero, one or two crew");
        s.xp(xp, true, false, true);
        s.xp(xp += 82, true, false, true);
        s.xp(xp += 257, true, false, false);
        expect(s.harvests() == 9, "duplicates, crew and missing context ignored");
        s.baseline(xp);
        expect(s.sorted() == 6 && s.harvests() == 9, "guide reset preserves totals");
        s.reset(xp);
        expect(s.sorted() == 0 && s.harvests() == 0, "explicit reset clears both");
        s.xp(xp += 96, true, true, false);
        s.xp(xp += 263, true, true, false);
        s.xp(xp += 258, true, false, true);
        expect(s.sorted() == 2 && s.harvests() == 1, "existing rounding tolerance retained");
        s.reset(-1);
        s.baselineIfMissing(xp);
        s.baselineIfMissing(xp + 500);
        s.xp(xp += 97, true, true, false);
        expect(
                s.sorted() == 1,
                "login baseline counts the first reward and never replaces a live baseline");
        System.out.println(
                "Session totals: reward, context, deduplication and reset checks passed.");
    }
}
