package com.lazysalvaging;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

import java.awt.Shape;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
final class GuideController {
    @Inject private Client client;
    @Inject private ItemManager itemManager;
    @Inject private LazySalvagingConfig config;
    final GuideEngine engine = new GuideEngine();
    final BagKnowledge bags = new BagKnowledge();
    final CargoCounter counter = new CargoCounter();
    final SessionTotals totals = new SessionTotals();
    private final SalvagingActivity salvagingActivity = new SalvagingActivity();
    private Map<Integer, Integer> transferBefore;
    private int transferTick, transferDirection;
    private boolean transferChanged, transferSawSnapshot;
    GuideEngine.Decision decision =
            new GuideEngine.Decision(
                    GuideEngine.Action.NONE, GuideEngine.Action.NONE, -1, "Waiting for login");
    private final Map<String, GameObject> objects = new HashMap<>();
    private final List<Widget> widgets = new ArrayList<>();
    private GuideEngine.View view = new GuideEngine.View();
    private int tick, lastSort = -100, depositTick = -100, depositBefore, cargoOpenSince = -1;
    private int pendingSeedId = -1, pendingSeedCount, pendingSeedTick;
    private boolean seedDepositBlocked;
    private int awaitingUntil = -1, seedBoxOpenSince = -1;
    private int lastExtractorHarvest = -100, alchReturnUntil = -1;
    private final ExtractorHarvest extractorHarvest = new ExtractorHarvest();
    private final BagFillTracker bagFill = new BagFillTracker();
    private boolean depleted;

    private String failure = "";
    private int failureTick;
    private String lastInventorySignature = "";
    private WorldView boatView;

    void startSession() {
        reset();
        resetTotals();
    }

    void reset() {
        pendingSeedId = -1;
        seedDepositBlocked = false;
        engine.reset();
        alchReturnUntil = -1;
        extractorHarvest.reset();
        bagFill.clear();
        bags.reset();
        salvagingActivity.reset();
        objects.clear();
        widgets.clear();
        tick = 0;
        lastSort = depositTick = -100;
        cargoOpenSince = -1;
        seedBoxOpenSince = -1;
        awaitingUntil = -1;
        lastExtractorHarvest = -100;
        depleted = false;
        failure = "";
        boatView = null;
        lastInventorySignature = "";
        counter.reset(
                client.getGameState() == GameState.LOGGED_IN
                        ? client.getSkillExperience(Skill.SAILING)
                        : -1);
        transferBefore = null;
        transferChanged = false;
        transferSawSnapshot = false;
        view = new GuideEngine.View();
        decision = waitFor("Open cargo hold to initialize a fresh session");
        totals.baseline(
                client.getGameState() == GameState.LOGGED_IN
                        ? client.getSkillExperience(Skill.SAILING)
                        : -1);
    }

    void resetTotals() {
        totals.reset(
                client.getGameState() == GameState.LOGGED_IN
                        ? client.getSkillExperience(Skill.SAILING)
                        : -1);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGIN_SCREEN
                || e.getGameState() == GameState.HOPPING
                || e.getGameState() == GameState.CONNECTION_LOST) {
            reset();
            if (e.getGameState() == GameState.LOGIN_SCREEN) resetTotals();
            decision = waitFor("Waiting for login / fresh cargo check");
        }
    }

    private GuideEngine.Decision waitFor(String text) {
        return new GuideEngine.Decision(GuideEngine.Action.NONE, GuideEngine.Action.NONE, -1, text);
    }

    @Subscribe
    public void onGameTick(GameTick e) {
        tick++;
        bags.tick(tick);
        if (client.getGameState() != GameState.LOGGED_IN) {
            decision = waitFor("Waiting for login");
            return;
        }
        counter.baselineIfMissing(client.getSkillExperience(Skill.SAILING));
        totals.baselineIfMissing(client.getSkillExperience(Skill.SAILING));
        scan();
        GuideEngine.View next = observe();
        if (!next.onBoat) {
            decision = engine.decide(next);
            return;
        }
        boolean openInventoryDuringSort =
                next.sorting
                        && !next.inventoryVisible
                        && engine.mode == GuideEngine.Mode.SORTING
                        && engine.stage == GuideEngine.Stage.PROCESS;
        if (tick <= awaitingUntil && !openInventoryDuringSort) {
            decision =
                    engine.withExtractorPreview(
                            next, waitFor("Waiting for the game to confirm your action"));
            view = next;
            return;
        }
        if (!failure.isEmpty() && tick - failureTick < 8) {
            decision = waitFor(failure);
            view = next;
            return;
        }
        view = next;
        decision = engine.decide(view);
        if (client.isWidgetSelected()
                && !next.alchReturning
                && !((next.alchSelected && decision.action == GuideEngine.Action.ALCH_ITEM)
                        || (next.selectedSeedSlot >= 0
                                && decision.action == GuideEngine.Action.USE_SEED_BOX)
                        || (next.selectedStoreSlot >= 0
                                && (decision.action == GuideEngine.Action.USE_GEM_BAG
                                        || decision.action == GuideEngine.Action.USE_HERB_BAG))))
            decision = waitFor("Cancel the selected spell or item to continue");
    }

    private void scan() {
        objects.clear();
        widgets.clear();
        Player p = client.getLocalPlayer();
        WorldView w = p == null ? null : p.getWorldView();
        if (boatView != null && w != boatView) {
            pendingSeedId = -1;
            seedDepositBlocked = false;
            engine.reset();
            alchReturnUntil = -1;
            extractorHarvest.reset();
            bagFill.clear();
            bags.reset();
            salvagingActivity.reset();
            depleted = false;
            lastExtractorHarvest = -100;
            cargoOpenSince = -1;
            seedBoxOpenSince = -1;
            counter.reset(client.getSkillExperience(Skill.SAILING));
            transferBefore = null;
        }
        boatView = w;
        if (w != null && w.getScene() != null) {
            Tile[][][] tiles = w.getScene().getTiles();
            int plane = w.getPlane();
            if (plane >= 0 && plane < tiles.length)
                for (Tile[] row : tiles[plane])
                    for (Tile tile : row) {
                        if (tile == null) continue;
                        for (GameObject o : tile.getGameObjects()) {
                            if (o == null) continue;
                            ObjectComposition c = client.getObjectDefinition(o.getId());
                            if (c.getImpostorIds() != null) c = c.getImpostor();
                            if (c == null) continue;
                            String name = clean(c.getName());
                            // Use the port hook (60510); exclude the starboard hook (60509).
                            if (name.equals("Dragon salvaging hook") && c.getId() != 60510)
                                continue;
                            if (name.equals("Dragon salvaging hook")
                                    || name.equals("Rosewood cargo hold")
                                    || name.equals("Salvaging station")
                                    || name.equals("Crystal extractor")) {
                                GameObject old = objects.get(name);
                                if (old == null
                                        || o.getLocalLocation().distanceTo(p.getLocalLocation())
                                                < old.getLocalLocation()
                                                        .distanceTo(p.getLocalLocation()))
                                    objects.put(name, o);
                            }
                        }
                    }
        }
        Set<Widget> seen = Collections.newSetFromMap(new IdentityHashMap<Widget, Boolean>());
        Widget[] roots = client.getWidgetRoots();
        if (roots != null) for (Widget root : roots) walk(root, seen, 0);
    }

    private void walk(Widget w, Set<Widget> seen, int depth) {
        if (w == null
                || depth > 30
                || seen.size() > 10000
                || !seen.add(w)
                || w.isHidden()
                || (w.getId() >>> 16) == 162) return;
        widgets.add(w);
        for (Widget[] a :
                new Widget[][] {
                    w.getDynamicChildren(), w.getStaticChildren(), w.getNestedChildren()
                }) if (a != null) for (Widget child : a) walk(child, seen, depth + 1);
    }

    private GuideEngine.View observe() {
        GuideEngine.View v = new GuideEngine.View();
        v.onBoat = hasFacilities();
        v.dropOverflow = config.dropOverflow();
        v.wreckAvailable = activeMerchantWreckInRange();
        v.seedSelectionInvalid = SeedSettings.selected(config).size() > 6;
        ItemContainer inv = client.getItemContainer(93);
        if (inv != null) {
            Item[] items = inv.getItems();
            String signature =
                    Arrays.toString(
                            Arrays.stream(items)
                                    .map(i -> i.getId() + ":" + i.getQuantity())
                                    .toArray());
            if (!signature.equals(lastInventorySignature)) {
                failure = "";
                lastInventorySignature = signature;
            }
            for (int slot = 0; slot < items.length; slot++) {
                Item i = items[slot];
                if (i.getId() < 0 || i.getQuantity() <= 0) continue;
                v.occupied++;
                if (i.getId() == 32861) v.salvage += i.getQuantity();
                String n = itemManager.getItemComposition(i.getId()).getName();
                GuideEngine.Kind k =
                        itemManager.getItemComposition(i.getId()).getNote() != -1
                                ? GuideEngine.Kind.OTHER
                                : LootPolicy.kind(n, config);
                v.inventory.add(
                        new GuideEngine.Entry(
                                slot, i.getId(), i.getQuantity(), n, k, bags.fit(k, n)));
                if (i.getId() == 33395 || i.getId() == 33393) v.gemsPresent = true;
                if (i.getId() == 33137 || i.getId() == 33135) v.herbsPresent = true;
                if (i.getId() == 24482 || i.getId() == 13639) v.seedsPresent = true;
            }
        }
        if (!v.gemsPresent) bags.invalidate(GuideEngine.Kind.GEM);
        if (!v.herbsPresent) bags.invalidate(GuideEngine.Kind.HERB);
        if (!v.seedsPresent) bags.invalidate(GuideEngine.Kind.SEED);
        if (depositTick >= 0 && tick - depositTick <= 8 && v.salvage < depositBefore) {
            engine.deposited();
            depositTick = -100;
            awaitingUntil = -1;
        }
        // Banking can empty bags without a normal inventory Fill/Empty operation.
        if (visible(client.getWidget(12, 0))) {
            bagFill.cancel(bags);
            bags.invalidate(GuideEngine.Kind.GEM);
            bags.invalidate(GuideEngine.Kind.HERB);
            bags.invalidate(GuideEngine.Kind.SEED);
        }
        BagFillTracker.Result fillResult = bagFill.observe(namedInventory(inv), bags, tick);
        if (fillResult == BagFillTracker.Result.CONFIRMED) awaitingUntil = -1;
        else if (fillResult == BagFillTracker.Result.UNCERTAIN) {
            awaitingUntil = -1;
            failure = "Bag transfer uncertain; Check the container again";
            failureTick = tick;
        }
        if (pendingSeedId >= 0 && inv != null) {
            if (inv.count(pendingSeedId) < pendingSeedCount) {
                pendingSeedId = -1;
                seedDepositBlocked = false;
                awaitingUntil = -1;
                bags.invalidate(GuideEngine.Kind.SEED);
            } else if (tick - pendingSeedTick > 6) {
                seedDepositBlocked = true;
                pendingSeedId = -1;
            }
        }
        v.seedDepositBlocked = seedDepositBlocked;
        finishTransfer(inv);
        Widget cargo = client.getWidget(943, 4);
        v.cargoOpen = visible(cargo);
        if (v.cargoOpen) {
            if (cargoOpenSince < 0) cargoOpenSince = tick;
            if (tick > cargoOpenSince) {
                int displayed = parseCount(cargo.getText());
                List<ItemContainer> matches = new ArrayList<>();
                for (ItemContainer c : client.getItemContainers()) {
                    int base = c.getId() & 32767;
                    if (base >= 963 && base <= 967 && occupied(c) == displayed) matches.add(c);
                }
                if (matches.size() == 1) {
                    v.cargoFresh = true;
                    v.cargoSlots = displayed;
                    v.cargoSalvage = matches.get(0).count(32861);
                    counter.observe(v.cargoSlots);
                    if (transferBefore != null) transferSawSnapshot = true;
                }
            }
        } else cargoOpenSince = -1;
        v.cargoKnown = counter.known();
        if (!v.cargoFresh && v.cargoKnown) v.cargoSlots = counter.slots();
        v.seedBoxOpen = visible(client.getWidget(128, 2));
        if (v.seedBoxOpen) {
            if (seedBoxOpenSince < 0) {
                seedBoxOpenSince = tick;
                bags.invalidate(GuideEngine.Kind.SEED);
            }
            // Allow the interface/container update to finish before using the cached container.
            ItemContainer seeds = client.getItemContainer(573);
            if (tick > seedBoxOpenSince && seeds != null)
                bags.visibleSeedContents(seedContents(seeds));
            v.seedBoxKnown = bags.seedsKnown;
        } else seedBoxOpenSince = -1;
        v.inventoryVisible = visible(client.getWidget(149, 0));
        v.magicVisible = visible(client.getWidget(218, 0));
        v.sailingVisible = visible(client.getWidget(937, 0));
        v.facilitiesVisible = visible(child(61407257, 47));
        Widget title = child(61407238, 0);
        v.assignmentPicker =
                visible(title) && clean(title.getText()).contains("Dragon salvaging hook");
        // Crew assignment widgets for the supported boat layout.
        v.crewAssigned = client.getVarbitValue(19240) == 14 && client.getVarbitValue(19480) == 14;
        Player player = client.getLocalPlayer();
        if (player != null && player.getAnimation() == 13583) depleted = false;
        if (player != null && player.getAnimation() == 13178) lastExtractorHarvest = tick;
        v.salvaging = salvagingActivity.observe(player == null ? -1 : player.getAnimation());
        v.sorting = player != null && (player.getAnimation() == 13599 || tick - lastSort <= 3);
        v.depleted = depleted;
        v.trackExtractor = config.trackExtractor();
        GameObject extractor = objects.get("Crystal extractor");
        if (extractor != null) {
            ObjectComposition definition = client.getObjectDefinition(extractor.getId());
            if (definition.getImpostorIds() != null) definition = definition.getImpostor();
            v.extractorInactive = definition != null && hasObjectAction(definition, "Activate");
            Renderable renderable = extractor.getRenderable();
            int animation =
                    renderable instanceof DynamicObject
                                    && ((DynamicObject) renderable).getAnimation() != null
                            ? ((DynamicObject) renderable).getAnimation().getId()
                            : -1;
            v.extractorReady = !v.extractorInactive && animation == 13177;
            v.extractorBusy =
                    player != null
                            && (player.getAnimation() == 13176 || player.getAnimation() == 13178);
        }
        boolean harvestAnimation = player != null && player.getAnimation() == 13178;
        boolean observedReady = v.extractorReady;
        if (extractorHarvest.confirmed()) {
            v.extractorReady = false;
            v.extractorBusy = player != null && player.getAnimation() == 13176;
        }
        extractorHarvest.observe(observedReady, harvestAnimation, tick);
        Widget selected = client.getSelectedWidget();
        v.alchSelected =
                client.isWidgetSelected() && selected != null && selected.getId() == 14286895;
        if (client.isWidgetSelected()
                && selected != null
                && selected.getId() == (149 << 16)
                && inv != null) {
            int slot = selected.getIndex();
            if (slot >= 0
                    && slot < inv.getItems().length
                    && inv.getItems()[slot].getId() == selected.getItemId())
                for (GuideEngine.Entry i : v.inventory)
                    if (i.slot == slot) {
                        if (i.kind == GuideEngine.Kind.SELECTED_SEED) v.selectedSeedSlot = slot;
                        if (i.kind == GuideEngine.Kind.GEM || i.kind == GuideEngine.Kind.HERB)
                            v.selectedStoreSlot = slot;
                    }
        }
        // The game returns to the spellbook after casting on an item; suppress a transient
        // Magic-tab prompt.
        v.alchReturning = tick <= alchReturnUntil && !v.magicVisible;
        if (v.magicVisible) alchReturnUntil = -1;
        v.alchBlocked = alchBlocked(inv);
        List<GuideEngine.Entry> refreshed = new ArrayList<>();
        for (GuideEngine.Entry i : v.inventory)
            refreshed.add(
                    new GuideEngine.Entry(
                            i.slot, i.id, i.quantity, i.name, i.kind, bags.fit(i.kind, i.name)));
        v.inventory = refreshed;
        return v;
    }

    private static int occupied(ItemContainer c) {
        int n = 0;
        for (Item i : c.getItems()) if (i.getId() >= 0 && i.getQuantity() > 0) n++;
        return n;
    }

    private static int parseCount(String s) {
        try {
            return Integer.parseInt(clean(s).replace(",", "").trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String alchBlocked(ItemContainer inv) {
        if (client.getBoostedSkillLevel(Skill.MAGIC) < 55) return "Magic level below 55";
        if (client.getVarbitValue(VarbitID.SPELLBOOK) != 0)
            return "switch to the standard spellbook";
        Map<Integer, Long> runes = new HashMap<>();
        boolean pouch = false;
        if (inv != null)
            for (Item i : inv.getItems())
                if (i.getId() >= 0) {
                    runes.merge(i.getId(), (long) i.getQuantity(), Long::sum);
                    if (itemManager
                            .getItemComposition(i.getId())
                            .getName()
                            .toLowerCase(Locale.ROOT)
                            .contains("rune pouch")) pouch = true;
                }
        if (pouch) {
            int[] types = {29, 1622, 1623, 14285};
            int[] quantities = {1624, 1625, 1626, 14286};
            EnumComposition mapping = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            for (int i = 0; i < 4; i++) {
                int type = client.getVarbitValue(types[i]);
                if (type > 0)
                    runes.merge(
                            mapping.getIntValue(type),
                            (long) client.getVarbitValue(quantities[i]),
                            Long::sum);
            }
        }
        if (runes.getOrDefault(561, 0L) < 1) return "no nature rune available";
        boolean staff = false;
        ItemContainer equipment = client.getItemContainer(94);
        if (equipment != null) {
            Item weapon = equipment.getItem(3);
            if (weapon != null && weapon.getId() >= 0) {
                String name =
                        itemManager
                                .getItemComposition(weapon.getId())
                                .getName()
                                .toLowerCase(Locale.ROOT);
                staff =
                        name.equals("staff of fire")
                                || name.equals("fire battlestaff")
                                || name.equals("mystic fire staff")
                                || name.equals("lava battlestaff")
                                || name.equals("mystic lava staff")
                                || name.equals("steam battlestaff")
                                || name.equals("mystic steam staff")
                                || name.equals("smoke battlestaff")
                                || name.equals("mystic smoke staff");
            }
        }
        long fire =
                runes.getOrDefault(554, 0L)
                        + runes.getOrDefault(4694, 0L)
                        + runes.getOrDefault(4697, 0L)
                        + runes.getOrDefault(4699, 0L);
        if (!staff && fire < 5) return "need five fire runes or a supported fire-rune staff";
        return "";
    }

    @Subscribe
    public void onChatMessage(ChatMessage e) {
        if (!(e.getType().name().equals("GAMEMESSAGE") || e.getType().name().equals("SPAM")))
            return;
        String text = clean(e.getMessage());
        bags.message(e.getMessage(), tick);
        if (text.contains("reclaimed by the sea")) {
            depleted = true;
            salvagingActivity.reset();
        }
        if (text.contains("start operating the salvaging hook")
                || text.contains("cast out your salvaging hook")
                || text.contains("You reel in some salvage")) depleted = false;
        if (text.contains("begin sorting through")
                || text.contains("You sort through the opulent salvage")) {
            lastSort = tick;
            bags.invalidate(GuideEngine.Kind.SEED);
        }
        if (text.contains("no more salvage to sort")) lastSort = -100;
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("not enough") && lower.contains("rune")) {
            failure = "Alchemy paused: " + text;
            failureTick = tick;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged e) {
        if (e.getContainerId() == 93 && transferBefore != null) transferChanged = true;
        if (e.getContainerId() == 573) {
            bags.seedContents(seedContents(e.getItemContainer()), tick);
        }
    }

    private Map<String, Integer> namedInventory(ItemContainer inventory) {
        Map<String, Integer> items = new HashMap<>();
        if (inventory != null)
            for (Item item : inventory.getItems())
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    ItemComposition definition = itemManager.getItemComposition(item.getId());
                    String name = definition.getName().toLowerCase(Locale.ROOT);
                    // Keep noted items distinct: they cannot be deposited into these bags.
                    if (definition.getNote() != -1) name = "noted:" + name;
                    items.merge(name, item.getQuantity(), Integer::sum);
                }
        return items;
    }

    private Map<String, Integer> seedContents(ItemContainer container) {
        Map<String, Integer> contents = new HashMap<>();
        for (Item i : container.getItems())
            if (i.getId() >= 0 && i.getQuantity() > 0)
                contents.put(
                        itemManager
                                .getItemComposition(i.getId())
                                .getName()
                                .toLowerCase(Locale.ROOT),
                        i.getQuantity());
        return contents;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked e) {
        String target = clean(e.getMenuTarget()), option = clean(e.getMenuOption());
        String lower = option.toLowerCase(Locale.ROOT);
        int group = e.getParam1() >>> 16;
        if (e.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
                && e.getParam1() == (149 << 16)
                && (view.alchSelected
                        || (option.equals("Cast")
                                && target.toLowerCase(Locale.ROOT).contains("high level alchemy"))))
            alchReturnUntil = tick + 3;
        if (!option.equals("Examine") && !option.equals("Cancel")) {
            bagFill.cancel(bags);
            extractorHarvest.cancelPending();
        }
        boolean cargoAction =
                (target.equals("Rosewood cargo hold") || group == 943 || group == 944)
                        && (lower.startsWith("deposit") || lower.startsWith("withdraw"));
        if (cargoAction) {
            if (transferBefore != null) counter.uncertain("Overlapping cargo actions; reopen hold");
            transferBefore = inventoryMap(client.getItemContainer(93));
            transferTick = tick;
            transferDirection = lower.startsWith("deposit") ? 1 : -1;
            transferChanged = false;
            transferSawSnapshot = false;
        } else if (transferBefore != null && !lower.equals("cancel") && !lower.equals("examine")) {
            counter.uncertain("Cargo action interrupted; reopen hold");
            transferBefore = null;
        }

        if (target.equals("Crystal extractor")
                && (option.equals("Activate") || option.equals("Harvest"))) {
            awaitingUntil = tick + 6;
            if (option.equals("Harvest")) {
                lastExtractorHarvest = tick;
                extractorHarvest.start(tick, client.getSkillExperience(Skill.SAILING));
            }
        }
        if (e.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
                && e.getParam1() == (149 << 16)
                && bagKind(e.getItemId()) == GuideEngine.Kind.SEED
                && decision.action == GuideEngine.Action.USE_SEED_BOX) {
            ItemContainer inv = client.getItemContainer(93);
            int slot = decision.slot;
            if (inv != null && slot >= 0 && slot < inv.getItems().length) {
                Item seed = inv.getItems()[slot];
                if (LootPolicy.kind(itemManager.getItemComposition(seed.getId()).getName(), config)
                        == GuideEngine.Kind.SELECTED_SEED) {
                    pendingSeedId = seed.getId();
                    pendingSeedCount = inv.count(pendingSeedId);
                    pendingSeedTick = tick;
                    seedDepositBlocked = false;
                    bags.invalidate(GuideEngine.Kind.SEED);
                    awaitingUntil = tick + 6;
                }
            }
        }
        GuideEngine.Kind bag = bagKind(e.getItemId());
        if (bag != GuideEngine.Kind.OTHER) {
            if (option.equals("Check")) {
                bags.request(bag, tick);
                awaitingUntil = tick + 2;
            } else if (option.equals("Fill")) {
                if (bag == GuideEngine.Kind.GEM || bag == GuideEngine.Kind.HERB)
                    bagFill.begin(bag, namedInventory(client.getItemContainer(93)), bags, tick);
                else bags.invalidate(bag);
                awaitingUntil = tick + 6;
            } else if (e.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
                    && bag != GuideEngine.Kind.SEED) {
                ItemContainer inv = client.getItemContainer(93);
                if ((decision.action == GuideEngine.Action.USE_GEM_BAG
                                        && bag == GuideEngine.Kind.GEM
                                || decision.action == GuideEngine.Action.USE_HERB_BAG
                                        && bag == GuideEngine.Kind.HERB)
                        && inv != null
                        && decision.slot >= 0
                        && decision.slot < inv.getItems().length) {
                    String name =
                            itemManager
                                    .getItemComposition(inv.getItems()[decision.slot].getId())
                                    .getName();
                    if (LootPolicy.kind(name, config) == bag)
                        bagFill.beginSingle(bag, name, namedInventory(inv), bags, tick);
                    else bags.invalidate(bag);
                    awaitingUntil = tick + 6;
                } else bags.invalidate(bag);
            } else if (option.equals("Empty") || option.equals("Destroy")) bags.invalidate(bag);
        }
        if (target.equals("Rosewood cargo hold") && option.equals("Deposit-all")) {
            depositTick = tick;
            depositBefore = view.salvage;
            awaitingUntil = tick + 2;
        }
        if (target.equals("Opulent salvage") && option.equals("Deposit-All")) {
            depositTick = tick;
            depositBefore = view.salvage;
            awaitingUntil = tick + 2;
        }
        if (option.equals("Withdraw-All") && target.equals("Opulent salvage"))
            awaitingUntil = tick + 2;
        if (option.equals("Sort-salvage") || option.equals("Deploy")) awaitingUntil = tick + 2;
    }

    @Subscribe
    public void onStatChanged(StatChanged e) {
        if (e.getSkill() != Skill.SAILING) return;
        Player player = client.getLocalPlayer();
        boolean here =
                client.getGameState() == GameState.LOGGED_IN
                        && player != null
                        && player.getWorldView() == boatView
                        && hasFacilities()
                        && engine.mode != GuideEngine.Mode.UNKNOWN;
        boolean harvesting = player != null && player.getAnimation() == 13178;
        if (harvesting) lastExtractorHarvest = tick;
        boolean harvestContext = harvesting || tick - lastExtractorHarvest <= 6;
        counter.xp(e.getXp(), here, harvestContext);
        boolean sortingContext =
                player != null
                        && (player.getAnimation() == 13599
                                || tick - lastSort <= 3
                                || engine.mode == GuideEngine.Mode.SORTING
                                        && engine.stage == GuideEngine.Stage.PROCESS);
        totals.xp(e.getXp(), here, sortingContext, harvestContext);
        if (here
                && extractorHarvest.xp(e.getXp(), tick)
                && awaitingUntil == extractorHarvest.deadline()) awaitingUntil = -1;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (e.getActor() == client.getLocalPlayer() && e.getActor().getAnimation() == 13178)
            lastExtractorHarvest = tick;
    }

    private boolean activeMerchantWreckInRange() {
        GameObject hook = objects.get("Dragon salvaging hook");
        WorldView sea = client.getTopLevelWorldView();
        if (hook == null || boatView == null || sea == null || sea.getScene() == null) return false;
        net.runelite.api.coords.LocalPoint position = hook.getLocalLocation();
        if (!boatView.isTopLevel()) {
            WorldEntity boat = sea.worldEntities().byIndex(boatView.getId());
            if (boat == null) return false;
            position = boat.transformToMainWorld(position);
        }
        if (position == null) return false;
        int x = position.getSceneX(), y = position.getSceneY(), plane = sea.getPlane();
        Tile[][][] tiles = sea.getScene().getTiles();
        if (plane < 0 || plane >= tiles.length) return false;
        for (int tx = Math.max(0, x - 8); tx <= x + 8 && tx < tiles[plane].length; tx++)
            for (int ty = Math.max(0, y - 8); ty <= y + 8 && ty < tiles[plane][tx].length; ty++) {
                Tile tile = tiles[plane][tx][ty];
                if (tile == null) continue;
                for (GameObject object : tile.getGameObjects()) {
                    if (object == null) continue;
                    ObjectComposition definition = client.getObjectDefinition(object.getId());
                    if (definition.getImpostorIds() != null) definition = definition.getImpostor();
                    if (definition != null
                            && WreckAvailability.reachable(
                                    definition.getId(),
                                    x,
                                    y,
                                    object.getLocalLocation().getSceneX(),
                                    object.getLocalLocation().getSceneY())) return true;
                }
            }
        return false;
    }

    private boolean hasFacilities() {
        return objects.containsKey("Dragon salvaging hook")
                && objects.containsKey("Rosewood cargo hold")
                && objects.containsKey("Salvaging station");
    }

    private static boolean hasObjectAction(ObjectComposition definition, String action) {
        String[] actions = definition.getActions();
        if (actions != null) for (String a : actions) if (action.equals(a)) return true;
        return false;
    }

    private static Map<Integer, Integer> inventoryMap(ItemContainer container) {
        Map<Integer, Integer> result = new HashMap<>();
        if (container != null)
            for (Item item : container.getItems())
                if (item.getId() >= 0 && item.getQuantity() > 0)
                    result.merge(item.getId(), item.getQuantity(), Integer::sum);
        return result;
    }

    private void finishTransfer(ItemContainer inventory) {
        if (transferBefore == null) return;
        if (tick - transferTick > 8) {
            counter.uncertain("Cargo transfer not confirmed; reopen hold");
            transferBefore = null;
            return;
        }
        if (!transferChanged) return;
        Map<Integer, Integer> after = inventoryMap(inventory);
        if (after.equals(transferBefore)) {
            transferChanged = false;
            return;
        }
        // Infer only nonstackable Opulent transfers. An open hold corrects all item types.
        counter.transfer(transferBefore, after, transferDirection, transferSawSnapshot);
        transferBefore = null;
        transferChanged = false;
    }

    private GuideEngine.Kind bagKind(int id) {
        if (id == 33395 || id == 33393) return GuideEngine.Kind.GEM;
        if (id == 33137 || id == 33135) return GuideEngine.Kind.HERB;
        if (id == 24482 || id == 13639) return GuideEngine.Kind.SEED;
        return GuideEngine.Kind.OTHER;
    }

    @Subscribe(priority = -1000)
    public void onPostMenuSort(PostMenuSort e) {
        if (!config.menuSwap()
                || client.getGameState() != GameState.LOGGED_IN
                || client.isMenuOpen()
                || client.isWidgetSelected()) return;
        MenuEntry[] entries = client.getMenu().getMenuEntries();
        if (entries.length == 0) return;
        int wreckWalk = ShipwreckMenuSwap.select(entries);
        if (wreckWalk >= 0) {
            BoatMenuGuard.promote(entries, wreckWalk);
            client.getMenu().setMenuEntries(entries);
            return;
        }
        Player player = client.getLocalPlayer();
        if (!view.onBoat
                || !hasFacilities()
                || client.getGameState() != GameState.LOGGED_IN
                || player == null
                || player.getWorldView() != boatView
                || client.isMenuOpen()
                || client.isWidgetSelected()) return;
        if (decision.action == GuideEngine.Action.SELECT_STORE_ITEM
                || decision.action == GuideEngine.Action.SELECT_SEED) {
            ItemContainer inventory = client.getItemContainer(93);
            int slot = decision.slot;
            if (inventory != null && slot >= 0 && slot < inventory.getItems().length) {
                Item item = inventory.getItems()[slot];
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    ItemComposition def = itemManager.getItemComposition(item.getId());
                    GuideEngine.Kind kind = LootPolicy.kind(def.getName(), config);
                    boolean allowed =
                            decision.action == GuideEngine.Action.SELECT_SEED
                                    ? kind == GuideEngine.Kind.SELECTED_SEED
                                    : kind == GuideEngine.Kind.GEM || kind == GuideEngine.Kind.HERB;
                    int index =
                            allowed && def.getNote() == -1
                                    ? DropMenuSwap.selectUse(entries, slot, item.getId())
                                    : -1;
                    if (index >= 0) {
                        DropMenuSwap.promote(entries, index);
                        client.getMenu().setMenuEntries(entries);
                        return;
                    }
                }
            }
        }
        GuideEngine.Kind checkBag =
                decision.action == GuideEngine.Action.CHECK_GEMS
                        ? GuideEngine.Kind.GEM
                        : decision.action == GuideEngine.Action.CHECK_HERBS
                                ? GuideEngine.Kind.HERB
                                : decision.action == GuideEngine.Action.CHECK_SEEDS
                                        ? GuideEngine.Kind.SEED
                                        : GuideEngine.Kind.OTHER;
        if (checkBag != GuideEngine.Kind.OTHER) {
            MenuEntry hovered = entries[entries.length - 1];
            int slot = hovered.getParam0();
            ItemContainer inventory = client.getItemContainer(93);
            if (hovered.getParam1() == DropMenuSwap.INVENTORY_WIDGET
                    && inventory != null
                    && slot >= 0
                    && slot < inventory.getItems().length) {
                Item item = inventory.getItems()[slot];
                int index =
                        DropMenuSwap.selectOption(
                                entries,
                                slot,
                                item.getId(),
                                item.getQuantity() > 0 && bagKind(item.getId()) == checkBag,
                                "Check");
                if (index >= 0) {
                    DropMenuSwap.promote(entries, index);
                    client.getMenu().setMenuEntries(entries);
                    return;
                }
            }
        }
        if (decision.action == GuideEngine.Action.DROP_ITEM) {
            MenuEntry hovered = entries[entries.length - 1];
            int slot = hovered.getParam0();
            ItemContainer inventory = client.getItemContainer(93);
            if (hovered.getParam1() == DropMenuSwap.INVENTORY_WIDGET
                    && inventory != null
                    && slot >= 0
                    && slot < inventory.getItems().length) {
                Item item = inventory.getItems()[slot];
                if (item.getId() >= 0 && item.getQuantity() > 0) {
                    String name = itemManager.getItemComposition(item.getId()).getName();
                    GuideEngine.Kind kind =
                            itemManager.getItemComposition(item.getId()).getNote() != -1
                                    ? GuideEngine.Kind.OTHER
                                    : LootPolicy.kind(name, config);
                    int index =
                            DropMenuSwap.select(
                                    entries, slot, item.getId(), dropAllowed(kind, name));
                    if (index >= 0) {
                        DropMenuSwap.promote(entries, index);
                        client.getMenu().setMenuEntries(entries);
                        return;
                    }
                }
            }
        }
        int index = BoatMenuGuard.select(entries, boatView.getId(), currentWorldAction(entries));
        if (index >= 0) {
            BoatMenuGuard.promote(entries, index);
            client.getMenu().setMenuEntries(entries);
        }
    }

    private int currentWorldAction(MenuEntry[] entries) {
        String name, option;
        switch (decision.action) {
            case DEPLOY:
                name = "Dragon salvaging hook";
                option = "Deploy";
                break;
            case DEPOSIT:
                name = "Rosewood cargo hold";
                option = "Deposit-all";
                break;
            case OPEN_CARGO:
                name = "Rosewood cargo hold";
                option = "Open";
                break;
            case SORT:
                name = "Salvaging station";
                option = "Sort-salvage";
                break;
            case ACTIVATE_EXTRACTOR:
                name = "Crystal extractor";
                option = "Activate";
                break;
            case HARVEST_EXTRACTOR:
                name = "Crystal extractor";
                option = "Harvest";
                break;
            default:
                return -1;
        }
        GameObject object = objects.get(name);
        if (object == null) return -1;
        ObjectComposition definition = client.getObjectDefinition(object.getId());
        if (definition.getImpostorIds() != null) definition = definition.getImpostor();
        Point min = object.getSceneMinLocation(), max = object.getSceneMaxLocation();
        for (int i = entries.length - 1; i >= 0; i--) {
            MenuEntry entry = entries[i];
            if (BoatMenuGuard.objectAction(entry.getType())
                    && entry.getWorldViewId() == boatView.getId()
                    && clean(entry.getTarget()).equals(name)
                    && entry.getOption().equals(option)
                    && (entry.getIdentifier() == object.getId()
                            || (definition != null && entry.getIdentifier() == definition.getId()))
                    && entry.getParam0() >= min.getX()
                    && entry.getParam0() <= max.getX()
                    && entry.getParam1() >= min.getY()
                    && entry.getParam1() <= max.getY()) return i;
        }
        return -1;
    }

    Shape shape(GuideEngine.Action action, int slot) {
        Widget w = null;
        switch (action) {
            case DEPLOY:
                return objectShape("Dragon salvaging hook");
            case DEPOSIT:
            case OPEN_CARGO:
                return objectShape("Rosewood cargo hold");
            case SORT:
                return objectShape("Salvaging station");
            case ACTIVATE_EXTRACTOR:
            case HARVEST_EXTRACTOR:
                return objectShape("Crystal extractor");
            case CLOSE_SEED_BOX:
                w = client.getWidget(128, 2);
                break;
            case CLOSE_CARGO:
                w = child(61800449, 11);
                break;
            case WITHDRAW:
                for (Widget q : widgets)
                    if (q.getId() == 61800458
                            && q.getItemId() == 32861
                            && hasAction(q, "Withdraw-All")) {
                        w = q;
                        break;
                    }
                break;
            case SAILING_TAB:
                w = actionWidget("Sailing Options");
                break;
            case INVENTORY_TAB:
                w = actionWidget("Inventory");
                break;
            case MAGIC_TAB:
                w = actionWidget("Magic");
                break;
            case FACILITIES_TAB:
                w = client.getWidget(937, 33);
                break;
            case ASSIGN_HOOK:
                w = child(61407257, 47);
                if (!hasAction(w, "Assign")) w = null;
                break;
            case JENKINS:
                Widget label = child(61407253, 14);
                if (label != null && clean(label.getText()).contains("Cabin Boy Jenkins"))
                    w = child(61407252, 3);
                break;
            case HIGH_ALCH:
                w = client.getWidget(14286895);
                break;
            case SELECT_STORE_ITEM:
            case SELECT_SEED:
            case ALCH_ITEM:
            case DROP_ITEM:
                return inventoryShape(slot);
            case USE_GEM_BAG:
            case CHECK_GEMS:
            case FILL_GEMS:
                return bagShape(GuideEngine.Kind.GEM);
            case USE_HERB_BAG:
            case CHECK_HERBS:
            case FILL_HERBS:
                return bagShape(GuideEngine.Kind.HERB);
            case USE_SEED_BOX:
            case CHECK_SEEDS:
            case FILL_SEEDS:
                return bagShape(GuideEngine.Kind.SEED);
            default:
                return null;
        }
        return visible(w) ? w.getBounds() : null;
    }

    private Shape objectShape(String name) {
        GameObject o = objects.get(name);
        if (o == null) return null;
        return o.getClickbox();
    }

    private Shape bagShape(GuideEngine.Kind kind) {
        for (GuideEngine.Entry i : view.inventory)
            if (bagKind(i.id) == kind) return inventoryShape(i.slot);
        return null;
    }

    List<Shape> dropShapes() {
        List<Shape> result = new ArrayList<>();
        if (decision.action != GuideEngine.Action.DROP_ITEM) return result;
        ItemContainer inventory = client.getItemContainer(93);
        if (inventory == null) return result;
        Item[] items = inventory.getItems();
        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];
            if (item.getId() < 0 || item.getQuantity() <= 0) continue;
            String name = itemManager.getItemComposition(item.getId()).getName();
            GuideEngine.Kind kind =
                    itemManager.getItemComposition(item.getId()).getNote() != -1
                            ? GuideEngine.Kind.OTHER
                            : LootPolicy.kind(name, config);
            if (dropAllowed(kind, name)) {
                Shape shape = inventoryShape(slot);
                if (shape != null) result.add(shape);
            }
        }
        return result;
    }

    private boolean dropAllowed(GuideEngine.Kind kind, String name) {
        GuideEngine.View live = new GuideEngine.View();
        live.dropOverflow = config.dropOverflow();
        ItemContainer inventory = client.getItemContainer(93);
        if (inventory != null)
            for (Item i : inventory.getItems())
                if (i.getQuantity() > 0) {
                    GuideEngine.Kind bag = bagKind(i.getId());
                    if (bag == GuideEngine.Kind.GEM) live.gemsPresent = true;
                    if (bag == GuideEngine.Kind.HERB) live.herbsPresent = true;
                    if (bag == GuideEngine.Kind.SEED) live.seedsPresent = true;
                }
        return LootPolicy.drop(
                new GuideEngine.Entry(-1, -1, 1, name, kind, bags.fit(kind, name)), live);
    }

    private Shape inventoryShape(int slot) {
        Widget inv = client.getWidget(149, 0);
        if (!visible(inv) || slot < 0) return null;
        Widget item = inv.getChild(slot);
        return visible(item) ? item.getBounds() : null;
    }

    private Widget actionWidget(String option) {
        for (Widget w : widgets) if (hasAction(w, option) && (w.getId() >>> 16) != 937) return w;
        return null;
    }

    private static boolean hasAction(Widget w, String action) {
        if (w == null || w.getActions() == null) return false;
        for (String s : w.getActions()) if (action.equals(s)) return true;
        return false;
    }

    private Widget child(int id, int index) {
        Widget p = client.getWidget(id);
        return p == null ? null : p.getChild(index);
    }

    private static boolean visible(Widget w) {
        return w != null && !w.isHidden();
    }

    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("<br\\s*/?>", " ").replaceAll("<[^>]*>", "");
    }
}
