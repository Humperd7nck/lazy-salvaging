package com.lazysalvaging;

public final class ExtractorPreviewTest {
    private static int checks;

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    public static void main(String[] args) {
        GuideEngine g = new GuideEngine();
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.cargoKnown = true;
        v.cargoSlots = 100;
        v.salvaging = true;
        v.occupied = 10;
        v.trackExtractor = true;
        v.extractorReady = true;
        GuideEngine.Decision d = g.decide(v);
        check(
                d.action == GuideEngine.Action.NONE
                        && d.preview == GuideEngine.Action.HARVEST_EXTRACTOR,
                "harvest previews during active salvaging");
        v.occupied = 28;
        v.salvage = 20;
        d = g.decide(v);
        check(
                d.action == GuideEngine.Action.HARVEST_EXTRACTOR
                        && d.preview == GuideEngine.Action.NONE,
                "full inventory promotes extractor to current");
        g.mode = GuideEngine.Mode.SORTING;
        g.stage = GuideEngine.Stage.PROCESS;
        v.sorting = true;
        d = g.decide(v);
        check(
                d.action == GuideEngine.Action.INVENTORY_TAB
                        && d.preview == GuideEngine.Action.NONE,
                "Inventory is current action while sorting");
        check(
                g.stage == GuideEngine.Stage.PROCESS && v.sorting,
                "Inventory prompt preserves ongoing sorting stage");
        v.inventoryVisible = true;
        d = g.decide(v);
        check(
                d.action == GuideEngine.Action.NONE && d.preview == GuideEngine.Action.NONE,
                "Inventory open returns to waiting for sorting without extractor preview");
        v.salvage = 0;
        v.sorting = false;
        check(
                g.decide(v).action == GuideEngine.Action.HARVEST_EXTRACTOR,
                "finished sorting promotes extractor");
        g.stage = GuideEngine.Stage.CLEANUP;
        v.inventoryVisible = true;
        v.inventory.add(
                new GuideEngine.Entry(
                        4, 1, 1, "Silk", GuideEngine.Kind.DROP, GuideEngine.Fit.UNKNOWN));
        d = g.decide(v);
        check(
                d.action == GuideEngine.Action.DROP_ITEM
                        && d.slot == 4
                        && d.preview == GuideEngine.Action.NONE,
                "drop cleanup has no extractor preview");
        v.extractorReady = false;
        v.extractorInactive = true;
        d = g.decide(v);
        check(
                d.action == GuideEngine.Action.DROP_ITEM && d.preview == GuideEngine.Action.NONE,
                "activation does not preview in cleanup");
        GuideEngine.Decision waiting =
                new GuideEngine.Decision(
                        GuideEngine.Action.NONE, GuideEngine.Action.NONE, -1, "Confirming");
        check(
                g.withExtractorPreview(v, waiting).preview == GuideEngine.Action.NONE,
                "sorting confirmation has no extractor preview");
        g.mode = GuideEngine.Mode.ACTIVE;
        check(
                g.withExtractorPreview(v, waiting).preview == GuideEngine.Action.ACTIVATE_EXTRACTOR,
                "active confirmation retains preview");
        g.mode = GuideEngine.Mode.SORTING;
        v.extractorBusy = true;
        check(
                g.withExtractorPreview(v, waiting).preview == GuideEngine.Action.NONE,
                "no preview while extractor itself busy");
        v.extractorBusy = false;
        v.trackExtractor = false;
        check(
                g.decide(v).preview == GuideEngine.Action.NONE,
                "disabled tracking has no extractor preview");
        v.trackExtractor = true;
        v.extractorInactive = false;
        check(
                g.decide(v).preview == GuideEngine.Action.NONE,
                "no stale preview once no longer due");
        v.extractorReady = true;
        v.inventory.clear();
        check(
                g.decide(v).action == GuideEngine.Action.HARVEST_EXTRACTOR,
                "finish entire cleanup before current extractor");
        v.onBoat = false;
        check(g.decide(v).preview == GuideEngine.Action.NONE, "no off-boat preview");
        System.out.println("Extractor preview: " + checks + " checks passed.");
    }
}
