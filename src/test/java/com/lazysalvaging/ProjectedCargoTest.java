package com.lazysalvaging;

public final class ProjectedCargoTest {
    private static int checks;

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    private static GuideEngine.View view(int cargo, int salvage) {
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = true;
        v.wreckAvailable = true;
        v.cargoKnown = true;
        v.cargoSlots = cargo;
        v.salvage = salvage;
        v.occupied = 28;
        v.salvaging = true;
        return v;
    }

    public static void main(String[] args) {
        for (int cargo : new int[] {190, 191, 209}) {
            GuideEngine g = new GuideEngine();
            GuideEngine.View v = view(cargo, 20);
            check(
                    g.decide(v).action == GuideEngine.Action.SAILING_TAB,
                    "skip deposit at projected threshold " + cargo);
            check(
                    g.mode == GuideEngine.Mode.SORTING && v.cargoSlots == cargo,
                    "phase changes without inventing cargo slots");
            v.sailingVisible = true;
            v.facilitiesVisible = true;
            check(
                    g.decide(v).action == GuideEngine.Action.ASSIGN_HOOK,
                    "assign hook before sorting");
            v.assignmentPicker = true;
            check(g.decide(v).action == GuideEngine.Action.JENKINS, "assign Jenkins");
            v.crewAssigned = true;
            check(
                    g.decide(v).action == GuideEngine.Action.SORT,
                    "sort carried salvage without withdrawing");
        }
        GuideEngine g = new GuideEngine();
        GuideEngine.View v = view(189, 20);
        check(g.decide(v).action == GuideEngine.Action.DEPOSIT, "projected 209 still deposits");
        g = new GuideEngine();
        v = view(209, 1);
        check(
                g.decide(v).action == GuideEngine.Action.SAILING_TAB,
                "one salvage reaching exactly 210 switches");
        g = new GuideEngine();
        v = view(190, 20);
        v.occupied = 27;
        check(
                g.decide(v).action == GuideEngine.Action.NONE && g.mode == GuideEngine.Mode.ACTIVE,
                "finish current salvaging inventory first");
        check(
                g.decide(v).preview == GuideEngine.Action.SAILING_TAB,
                "preview phase transition instead of unnecessary deposit");
        v.occupied = 28;
        v.trackExtractor = true;
        v.extractorReady = true;
        check(
                g.decide(v).action == GuideEngine.Action.HARVEST_EXTRACTOR,
                "existing extractor checkpoint retained");
        v.extractorReady = false;
        check(
                g.decide(v).action == GuideEngine.Action.SAILING_TAB,
                "assignment follows extractor without deposit");
        g = new GuideEngine();
        v = view(209, 0);
        check(
                g.decide(v).action == GuideEngine.Action.NONE && g.mode == GuideEngine.Mode.ACTIVE,
                "other occupied inventory slots do not count as salvage deposits");
        g = new GuideEngine();
        v = view(190, 20);
        v.cargoKnown = false;
        check(
                g.decide(v).action == GuideEngine.Action.OPEN_CARGO,
                "unknown cargo must be checked first");
        g = new GuideEngine();
        v = view(190, 20);
        v.cargoOpen = true;
        v.cargoFresh = true;
        check(
                g.decide(v).action == GuideEngine.Action.CLOSE_CARGO,
                "close already open hold without deposit or withdrawal");
        System.out.println("Projected cargo: " + checks + " checks passed.");
    }
}
