package com.lazysalvaging;

import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.*;

import java.awt.*;

import javax.inject.Inject;

final class GuideHud extends OverlayPanel {
    private final GuideController guide;
    private final LazySalvagingConfig config;

    @Inject
    GuideHud(GuideController guide, LazySalvagingConfig config) {
        this.guide = guide;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(210, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.hud()) return null;
        panelComponent.getChildren().clear();
        panelComponent.setBackgroundColor(
                new Color(30, 30, 30, Math.max(0, Math.min(100, config.hudOpacity())) * 255 / 100));
        boolean active = guide.engine.mode == GuideEngine.Mode.ACTIVE;
        boolean sorting = guide.engine.mode == GuideEngine.Mode.SORTING;
        panelComponent
                .getChildren()
                .add(
                        LineComponent.builder()
                                .left("[ Salvaging ]")
                                .right("[ Sorting ]")
                                .leftColor(active ? config.current() : Color.GRAY)
                                .rightColor(sorting ? config.current() : Color.GRAY)
                                .build());
        panelComponent
                .getChildren()
                .add(
                        LineComponent.builder()
                                .left("Cargo hold:")
                                .right(
                                        (guide.counter.known()
                                                        ? Integer.toString(guide.counter.slots())
                                                        : "?")
                                                + " / 240")
                                .build());
        panelComponent
                .getChildren()
                .add(
                        LineComponent.builder()
                                .left("Total sorted:")
                                .right(Long.toString(guide.totals.sorted()))
                                .build());
        if (config.trackExtractor())
            panelComponent
                    .getChildren()
                    .add(
                            LineComponent.builder()
                                    .left("Crystal harvests:")
                                    .right(Long.toString(guide.totals.harvests()))
                                    .build());
        return super.render(graphics);
    }
}
