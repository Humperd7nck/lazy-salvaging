package com.lazysalvaging;

import java.util.ArrayList;
import java.util.List;

/** Pure decision logic. No client calls, menu mutation, or input execution. */
final class GuideEngine {
    enum Mode {
        UNKNOWN,
        ACTIVE,
        SORTING
    }

    enum Stage {
        PREPARE,
        ASSIGN,
        PROCESS,
        BEFORE_CLEANUP,
        CLEANUP,
        RECHECK
    }

    enum Kind {
        OTHER,
        ALCHEMY,
        DROP,
        GEM,
        HERB,
        SEED,
        SELECTED_SEED
    }

    enum Fit {
        UNKNOWN,
        YES,
        NO
    }

    enum Action {
        NONE,
        OPEN_CARGO,
        CLOSE_CARGO,
        CLOSE_SEED_BOX,
        WITHDRAW,
        DEPLOY,
        DEPOSIT,
        SORT,
        ACTIVATE_EXTRACTOR,
        HARVEST_EXTRACTOR,
        SAILING_TAB,
        FACILITIES_TAB,
        ASSIGN_HOOK,
        JENKINS,
        INVENTORY_TAB,
        MAGIC_TAB,
        HIGH_ALCH,
        ALCH_ITEM,
        SELECT_SEED,
        USE_SEED_BOX,
        SELECT_STORE_ITEM,
        USE_GEM_BAG,
        USE_HERB_BAG,
        DROP_ITEM,
        CHECK_GEMS,
        CHECK_HERBS,
        CHECK_SEEDS,
        FILL_GEMS,
        FILL_HERBS,
        FILL_SEEDS
    }

    static final class Entry {
        final int slot, id, quantity;
        final String name;
        final Kind kind;
        final Fit fit;

        Entry(int slot, int id, int quantity, String name, Kind kind, Fit fit) {
            this.slot = slot;
            this.id = id;
            this.quantity = quantity;
            this.name = name;
            this.kind = kind;
            this.fit = fit;
        }
    }

    static final class View {
        boolean seedBoxOpen, seedBoxKnown, wreckAvailable;
        boolean onBoat,
                cargoOpen,
                cargoFresh,
                cargoKnown,
                inventoryVisible,
                magicVisible,
                sailingVisible,
                facilitiesVisible,
                assignmentPicker,
                crewAssigned,
                salvaging,
                sorting,
                depleted,
                alchSelected,
                alchReturning,
                gemsPresent,
                herbsPresent,
                seedsPresent;
        int selectedSeedSlot = -1, selectedStoreSlot = -1;
        boolean dropOverflow;
        boolean seedSelectionInvalid, seedDepositBlocked;
        int cargoSlots = -1, cargoSalvage = -1, salvage, occupied;
        boolean trackExtractor, extractorInactive, extractorReady, extractorBusy;
        String alchBlocked = "";
        List<Entry> inventory = new ArrayList<>();
    }

    static final class Decision {
        final Action action, preview;
        final int slot;
        final String text;

        Decision(Action action, Action preview, int slot, String text) {
            this.action = action;
            this.preview = preview;
            this.slot = slot;
            this.text = text;
        }

        @Override
        public String toString() {
            return action + ":" + slot + ":" + text;
        }
    }

    Mode mode = Mode.UNKNOWN;
    Stage stage = Stage.PREPARE;
    boolean cargoRequired = true, assignRequired = true;

    void reset() {
        mode = Mode.UNKNOWN;
        stage = Stage.PREPARE;
        cargoRequired = true;
        assignRequired = true;
    }

    void deposited() {
        if (mode == Mode.ACTIVE) cargoRequired = true;
    }

    private Decision d(Action a, Action p, String text) {
        return new Decision(a, p, -1, text);
    }

    private Decision item(Action a, Entry i, String text) {
        return new Decision(a, Action.NONE, i.slot, text + " " + i.name);
    }

    Decision decide(View v) {
        return withExtractorPreview(v, decideCurrent(v));
    }

    Decision withExtractorPreview(View v, Decision current) {
        if (mode != Mode.ACTIVE)
            return current.preview == Action.ACTIVATE_EXTRACTOR
                            || current.preview == Action.HARVEST_EXTRACTOR
                    ? new Decision(current.action, Action.NONE, current.slot, current.text)
                    : current;
        if (!v.onBoat
                || !v.trackExtractor
                || v.extractorBusy
                || current.action == Action.ACTIVATE_EXTRACTOR
                || current.action == Action.HARVEST_EXTRACTOR) return current;
        Action due =
                v.extractorInactive
                        ? Action.ACTIVATE_EXTRACTOR
                        : v.extractorReady ? Action.HARVEST_EXTRACTOR : Action.NONE;
        return due == Action.NONE
                ? current
                : new Decision(current.action, due, current.slot, current.text);
    }

    private Decision decideCurrent(View v) {
        if (!v.onBoat)
            return d(
                    Action.NONE,
                    Action.NONE,
                    "Board your boat with the configured salvaging facilities");
        if (v.seedBoxOpen)
            return v.seedBoxKnown
                    ? d(Action.CLOSE_SEED_BOX, Action.NONE, "Close seed box to continue cleanup")
                    : d(Action.NONE, Action.NONE, "Waiting for seed box contents");
        for (int pass = 0; pass < 12; pass++) {
            if (mode == Mode.UNKNOWN || mode == Mode.ACTIVE) {
                cargoRequired = !(v.cargoKnown || v.cargoFresh);
                if (cargoRequired && !v.cargoOpen)
                    return d(
                            Action.OPEN_CARGO,
                            Action.NONE,
                            "Open cargo hold to initialize or refresh the counter");
                if (cargoRequired)
                    return d(Action.NONE, Action.NONE, "Waiting for current cargo contents");
                cargoRequired = false;
                if ((v.cargoSlots >= 210 || (v.occupied >= 28 && depositReachesSortingThreshold(v)))
                        && (mode == Mode.UNKNOWN || !v.salvaging || v.occupied >= 28)) {
                    mode = Mode.SORTING;
                    stage = Stage.PREPARE;
                    assignRequired = true;
                } else mode = Mode.ACTIVE;
            }
            if (mode == Mode.ACTIVE) {
                if (v.cargoOpen)
                    return d(
                            Action.CLOSE_CARGO,
                            v.wreckAvailable ? Action.DEPLOY : Action.NONE,
                            "Close cargo hold");
                if (v.occupied >= 28) {
                    Decision priority = extractor(v);
                    if (priority != null) return priority;
                    if (v.salvage == 0)
                        return d(
                                Action.NONE,
                                Action.NONE,
                                "Inventory full without salvage; make room to continue");
                    return d(Action.DEPOSIT, Action.NONE, "Deposit-all salvage into cargo hold");
                }
                if (!v.wreckAvailable) {
                    Decision priority = extractor(v);
                    if (priority != null) return priority;
                    return d(
                            Action.NONE,
                            Action.NONE,
                            "Waiting for an active Merchant shipwreck in range");
                }
                if (v.salvaging && !v.depleted)
                    return d(
                            Action.NONE,
                            depositReachesSortingThreshold(v) ? Action.SAILING_TAB : Action.DEPOSIT,
                            "Salvaging — wait for a full inventory");
                Decision priority = extractor(v);
                if (priority != null) return priority;
                if (v.depleted)
                    return d(
                            Action.DEPLOY,
                            Action.DEPOSIT,
                            "Wreck depleted: deploy the hook at the next wreck");
                return d(Action.DEPLOY, Action.DEPOSIT, "Deploy Dragon salvaging hook");
            }
            if (stage == Stage.PREPARE) {
                if (!v.cargoOpen) {
                    Decision priority = extractor(v);
                    if (priority != null) return priority;
                }
                if (v.occupied < 28) {
                    if (!v.cargoOpen)
                        return d(
                                Action.OPEN_CARGO,
                                Action.NONE,
                                "Open cargo hold to fill inventory with salvage");
                    if (!v.cargoFresh)
                        return d(Action.NONE, Action.NONE, "Waiting for current cargo contents");
                    if (v.cargoSalvage > 0)
                        return d(Action.WITHDRAW, Action.NONE, "Withdraw-All Opulent salvage");
                }
                stage = assignRequired ? Stage.ASSIGN : Stage.PROCESS;
                continue;
            }
            if (v.cargoOpen && stage != Stage.RECHECK)
                return d(Action.CLOSE_CARGO, Action.NONE, "Close cargo hold");
            if (stage == Stage.ASSIGN) {
                if (v.crewAssigned) {
                    assignRequired = false;
                    stage = Stage.PROCESS;
                    continue;
                }
                if (v.assignmentPicker)
                    return d(Action.JENKINS, Action.NONE, "Assign Cabin Boy Jenkins to the hook");
                if (!v.sailingVisible)
                    return d(Action.SAILING_TAB, Action.NONE, "Open Sailing Options");
                if (!v.facilitiesVisible)
                    return d(Action.FACILITIES_TAB, Action.NONE, "Open Facilities");
                return d(Action.ASSIGN_HOOK, Action.NONE, "Assign crew to Dragon salvaging hook");
            }
            if (stage == Stage.PROCESS) {
                if (v.salvage == 0) {
                    stage = Stage.BEFORE_CLEANUP;
                    continue;
                }
                if (v.sorting) {
                    if (!v.inventoryVisible)
                        return d(
                                Action.INVENTORY_TAB,
                                Action.NONE,
                                "Open Inventory while sorting continues");
                    Action preview = Action.NONE;
                    for (Entry i : v.inventory)
                        if (i.kind == Kind.GEM) {
                            preview = Action.CHECK_GEMS;
                            break;
                        }
                    return d(Action.NONE, preview, "Sorting salvage — wait for completion");
                }
                Decision priority = extractor(v);
                if (priority != null) return priority;
                return d(Action.SORT, Action.NONE, "Sort-salvage at the Salvaging station");
            }
            if (stage == Stage.BEFORE_CLEANUP) {
                Decision priority = extractor(v);
                if (priority != null) return priority;
                stage = Stage.CLEANUP;
                continue;
            }
            if (stage == Stage.CLEANUP) {
                Decision next = cleanup(v);
                if (next != null) return next;
                stage = Stage.RECHECK;
                continue;
            }
            if (stage == Stage.RECHECK) {
                Decision priority = extractor(v);
                if (priority != null) return priority;
                if (!(v.cargoKnown || v.cargoFresh) && !v.cargoOpen)
                    return d(
                            Action.OPEN_CARGO,
                            Action.NONE,
                            "Open cargo hold to refresh the counter");
                if (!(v.cargoKnown || v.cargoFresh))
                    return d(Action.NONE, Action.NONE, "Waiting for current cargo contents");
                if (v.cargoSlots >= 15) {
                    stage = Stage.PREPARE;
                    continue;
                }
                mode = Mode.ACTIVE;
                cargoRequired = false;
                continue;
            }
        }
        return d(Action.NONE, Action.NONE, "Waiting for inventory space or a fresh observation");
    }

    private boolean depositReachesSortingThreshold(View v) {
        return v.salvage > 0 && v.cargoSlots >= 0 && (long) v.cargoSlots + v.salvage >= 210;
    }

    private Decision extractor(View v) {
        if (!v.trackExtractor) return null;
        if (v.extractorBusy)
            return d(Action.NONE, Action.NONE, "Crystal extractor — wait for completion");
        Action action =
                v.extractorInactive
                        ? Action.ACTIVATE_EXTRACTOR
                        : v.extractorReady ? Action.HARVEST_EXTRACTOR : Action.NONE;
        if (action == Action.NONE) return null;
        if (v.cargoOpen)
            return d(Action.CLOSE_CARGO, action, "Close cargo hold to tend the crystal extractor");
        return d(
                action,
                Action.NONE,
                action == Action.ACTIVATE_EXTRACTOR
                        ? "Activate Crystal extractor"
                        : "Harvest Crystal extractor");
    }

    private Decision bag(
            View v, Kind kind, boolean present, Action check, Action fill, String label) {
        Entry unknown = null, fitting = null;
        for (Entry i : v.inventory)
            if (i.kind == kind) {
                if (i.fit == Fit.UNKNOWN && unknown == null) unknown = i;
                if (i.fit == Fit.YES && fitting == null) fitting = i;
            }
        if (unknown == null && fitting == null) return null;
        if (!present) return null; // Keep stored items when their container is missing.
        if (!v.inventoryVisible) return d(Action.INVENTORY_TAB, Action.NONE, "Open Inventory");
        if (unknown != null)
            return d(check, Action.NONE, "Check " + label + " to read its contents");
        for (Entry i : v.inventory)
            if (i.kind != kind
                    && BagFillTracker.accepts(kind, i.name.toLowerCase(java.util.Locale.ROOT))) {
                Action use = kind == Kind.GEM ? Action.USE_GEM_BAG : Action.USE_HERB_BAG;
                if (v.selectedStoreSlot == fitting.slot)
                    return new Decision(
                            use, Action.NONE, fitting.slot, "Use " + fitting.name + " on " + label);
                return new Decision(
                        Action.SELECT_STORE_ITEM, use, fitting.slot, "Use " + fitting.name);
            }
        return d(fill, Action.NONE, "Fill " + label);
    }

    private Decision cleanup(View v) {
        if (v.alchReturning)
            return d(
                    Action.NONE,
                    Action.NONE,
                    "Waiting for High Level Alchemy to return to the spellbook");
        if (v.seedSelectionInvalid)
            return d(Action.NONE, Action.NONE, "Choose at most 6 seeds in Seed Box settings");
        Decision result =
                bag(v, Kind.GEM, v.gemsPresent, Action.CHECK_GEMS, Action.FILL_GEMS, "gem sack");
        if (result != null) return result;
        result =
                bag(
                        v,
                        Kind.HERB,
                        v.herbsPresent,
                        Action.CHECK_HERBS,
                        Action.FILL_HERBS,
                        "silklined herb sack");
        if (result != null) return result;
        for (Entry i : v.inventory)
            if (i.kind == Kind.ALCHEMY) {
                if (!v.alchBlocked.isEmpty())
                    return d(Action.NONE, Action.NONE, "Alchemy paused: " + v.alchBlocked);
                if (v.alchSelected) return item(Action.ALCH_ITEM, i, "High Level Alchemy:");
                if (!v.magicVisible)
                    return d(Action.MAGIC_TAB, Action.NONE, "Open Magic for High Level Alchemy");
                return d(Action.HIGH_ALCH, Action.NONE, "Select High Level Alchemy");
            }
        // Return to Inventory even if cleanup has no remaining item actions.
        if (v.magicVisible || v.alchSelected)
            return d(
                    Action.INVENTORY_TAB,
                    Action.NONE,
                    "Return to Inventory; cancel selected spell if necessary");
        for (Entry i : v.inventory)
            if (LootPolicy.drop(i, v)) {
                if (!v.inventoryVisible)
                    return d(Action.INVENTORY_TAB, Action.NONE, "Open Inventory");
                return item(Action.DROP_ITEM, i, "Drop:");
            }
        for (Entry i : v.inventory)
            if (i.kind == Kind.SELECTED_SEED) {
                if (!v.seedsPresent || i.fit == Fit.NO) continue;
                if (!v.inventoryVisible)
                    return d(Action.INVENTORY_TAB, Action.NONE, "Open Inventory");
                if (v.seedDepositBlocked) {
                    if (i.fit == Fit.UNKNOWN)
                        return d(
                                Action.CHECK_SEEDS,
                                Action.NONE,
                                "Check seed box to confirm space after the unconfirmed deposit");
                    return d(
                            Action.NONE,
                            Action.NONE,
                            "Seed deposit not confirmed: make room in the box, then reset guide");
                }
                if (v.selectedSeedSlot == i.slot)
                    return new Decision(
                            Action.USE_SEED_BOX,
                            Action.NONE,
                            i.slot,
                            "Use " + i.name + " on seed box");
                return new Decision(
                        Action.SELECT_SEED, Action.USE_SEED_BOX, i.slot, "Use " + i.name);
            }
        return null;
    }

    static boolean isBag(Kind kind) {
        return kind == Kind.GEM || kind == Kind.HERB || kind == Kind.SEED;
    }
}
