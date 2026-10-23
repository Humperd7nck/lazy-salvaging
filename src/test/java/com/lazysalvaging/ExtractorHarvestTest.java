package com.lazysalvaging;

public final class ExtractorHarvestTest {
    private static int checks;

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    public static void main(String[] args) {
        ExtractorHarvest h = new ExtractorHarvest();
        h.start(10, 1000);
        check(!h.confirmed() && h.deadline() == 16, "click alone retains fallback wait");
        check(!h.xp(1082, 11), "crew-only reward does not end harvest wait");
        check(h.xp(1339, 12) && h.confirmed(), "harvest reward confirms before timeout");
        h.observe(true, true, 12);
        check(h.confirmed(), "confirmation overrides lingering ready/animation");
        check(!h.xp(1339, 12), "duplicate total does not confirm again");
        h.cancelPending();
        check(h.confirmed(), "next action does not restore stale harvest animation wait");
        h.observe(false, true, 13);
        check(h.confirmed(), "animation tail remains suppressed");
        h.observe(false, false, 14);
        check(!h.confirmed(), "clear after extractor and animation settle");
        for (int crew = 0; crew < 3; crew++) {
            h.start(20, 2000);
            check(h.xp(2257 + 82 * crew, 21), "combined harvest and crew reward " + crew);
        }
        h.start(30, 3000);
        check(!h.xp(3098, 31) && !h.confirmed(), "sorting reward not harvest");
        h.observe(false, false, 31);
        check(!h.confirmed(), "animation absence alone does not confirm success");
        h.start(40, 4000);
        h.cancelPending();
        check(!h.xp(4257, 41), "interrupted request cannot release another action's wait");
        h.start(50, 5000);
        check(!h.xp(5257, 57), "late reward falls back to existing wait behavior");
        h.start(60, 6000);
        h.xp(6257, 61);
        h.reset();
        check(!h.confirmed(), "session reset clears completion");
        System.out.println("Extractor harvest: " + checks + " checks passed.");
    }
}
