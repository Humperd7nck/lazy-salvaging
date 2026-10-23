package com.lazysalvaging;

import com.google.gson.Gson;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.concurrent.*;

import javax.imageio.ImageIO;

/** Exercise actual RuneLite config/default handling and the rendered info panel. */
public final class SettingsIntegrationTest {
    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            Constructor<?> ctor = ConfigManager.class.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            ConfigManager manager =
                    (ConfigManager)
                            ctor.newInstance(
                                    null,
                                    executor,
                                    new EventBus(),
                                    null,
                                    new Gson(),
                                    null,
                                    null,
                                    null);
            Class<?> dataClass = Class.forName("net.runelite.client.config.ConfigData");
            Constructor<?> dataCtor = dataClass.getDeclaredConstructor(File.class);
            dataCtor.setAccessible(true);
            File temp = Files.createTempFile("lazy-config-test", ".properties").toFile();
            temp.deleteOnExit();
            set(manager, "configProfile", dataCtor.newInstance(temp));
            manager.setConfiguration("lazysalvaging", "seedIrit", true);
            manager.setConfiguration("lazysalvaging", "current", Color.GREEN);
            manager.setConfiguration("lazysalvaging", "cargoSwap", false);
            manager.setConfiguration("lazysalvaging", "dropSwap", false);
            manager.setConfiguration("lazysalvaging", "preventBoatMisclicks", false);
            LazySalvagingConfig config = manager.getConfig(LazySalvagingConfig.class);
            manager.setDefaultConfiguration(config, false);
            expect(
                    config.seedIrit() && config.current().equals(Color.GREEN),
                    "existing choices survive defaults");
            expect(!config.menuSwap(), "old all-off menu settings migrate");
            config.generallootHeader();
            config.seedsHeader();
            config.herbsHeader();
            config.gemsHeader();
            expect(
                    manager.getConfigDescriptor(config).getSections().size() == 3,
                    "native config loads three top-level sections");
            manager.setConfiguration("lazysalvaging", "lootTiara", LootAction.KEEP);
            manager.setDefaultConfiguration(config, false);
            expect(config.lootTiara() == LootAction.KEEP, "custom loot choice survives reload");
            GuideController guide = new GuideController();
            set(guide, "config", config);
            // No client is injected: disabled swaps must return before touching any client/menu
            // state.
            guide.onPostMenuSort(new PostMenuSort());
            guide.counter.observe(201);
            guide.engine.mode = GuideEngine.Mode.SORTING;
            guide.totals.reset(1000);
            guide.totals.xp(1179, true, true, false);
            guide.totals.xp(1436, true, false, true);
            manager.setConfiguration("lazysalvaging", "trackExtractor", true);
            GuideHud hud = new GuideHud(guide, config);
            hud.setClearChildren(false);
            BufferedImage image = new BufferedImage(260, 140, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(70, 75, 65));
            g.fillRect(0, 0, 260, 140);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            hud.render(g);
            Dimension size = hud.render(g);
            g.dispose();
            expect(size.width <= 230 && size.height <= 110, "compact info panel dimensions");
            Field pf = OverlayPanel.class.getDeclaredField("panelComponent");
            pf.setAccessible(true);
            PanelComponent panel = (PanelComponent) pf.get(hud);
            expect(panel.getChildren().size() == 4, "phase, cargo and two totals only");
            File preview = new File("build/info-panel-preview.png");
            preview.getParentFile().mkdirs();
            ImageIO.write(image, "png", preview);
            manager.setConfiguration("lazysalvaging", "trackExtractor", false);
            manager.setConfiguration("lazysalvaging", "hudOpacity", 0);
            g = image.createGraphics();
            hud.render(g);
            g.dispose();
            expect(
                    panel.getChildren().size() == 3 && panel.getBackgroundColor().getAlpha() == 0,
                    "extractor row conditional, opacity applies to background");
            // The same controller instance is reused when the plugin is toggled off/on.
            GameState[] state = {GameState.LOGGED_IN};
            int[] xp = {1436};
            Client client =
                    (Client)
                            Proxy.newProxyInstance(
                                    Client.class.getClassLoader(),
                                    new Class<?>[] {Client.class},
                                    (proxy, method, arguments) -> {
                                        if (method.getName().equals("getGameState"))
                                            return state[0];
                                        if (method.getName().equals("getSkillExperience"))
                                            return xp[0];
                                        throw new AssertionError(
                                                "Unexpected restart API: " + method.getName());
                                    });
            set(guide, "client", client);
            guide.decision =
                    new GuideEngine.Decision(
                            GuideEngine.Action.DROP_ITEM,
                            GuideEngine.Action.USE_SEED_BOX,
                            3,
                            "Previous cleanup target");
            guide.bags.gemsKnown = true;
            set(guide, "seedDepositBlocked", true);
            guide.startSession();
            expect(
                    guide.engine.mode == GuideEngine.Mode.UNKNOWN && !guide.counter.known(),
                    "restart requires fresh phase and cargo reading");
            expect(
                    guide.decision.action == GuideEngine.Action.NONE
                            && guide.decision.preview == GuideEngine.Action.NONE
                            && guide.decision.slot == -1,
                    "restart clears stale current and preview targets immediately");
            expect(
                    !guide.bags.gemsKnown
                            && guide.totals.sorted() == 0
                            && guide.totals.harvests() == 0,
                    "restart clears capacity knowledge and totals");
            guide.totals.xp(xp[0] += 97, true, true, false);
            expect(guide.totals.sorted() == 1, "first reward after restart uses fresh baseline");
            state[0] = GameState.LOGIN_SCREEN;
            guide.startSession();
            expect(
                    guide.totals.sorted() == 0 && guide.engine.mode == GuideEngine.Mode.UNKNOWN,
                    "enabling before login also starts clean");
            expect(
                    config.seedIrit() && config.lootTiara() == LootAction.KEEP,
                    "restart preserves regular settings");
            System.out.println(
                    "Settings integration: native defaults, migration, menu-off gate, info panel"
                        + " and clean restart passed.");
        } finally {
            executor.shutdownNow();
        }
    }
}
