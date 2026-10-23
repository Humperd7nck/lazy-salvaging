package com.lazysalvaging;

import net.runelite.client.ui.overlay.*;

import java.awt.*;

import javax.inject.Inject;

final class GuideOverlay extends Overlay {
    private final GuideController guide;
    private final LazySalvagingConfig config;

    @Inject
    GuideOverlay(GuideController guide, LazySalvagingConfig config) {
        this.guide = guide;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D g) {
        GuideEngine.Decision d = guide.decision;
        Shape current = guide.shape(d.action, d.slot);
        Shape next = config.preview() ? guide.shape(d.preview, -1) : null;
        if (next != null && (current == null || !next.getBounds().equals(current.getBounds())))
            draw(g, next, config.next());
        if (d.action == GuideEngine.Action.DROP_ITEM) {
            for (Shape shape : guide.dropShapes()) draw(g, shape, config.drop());
            return null;
        }
        Color color;
        switch (d.action) {
            case INVENTORY_TAB:
                color = config.inventoryTabColor();
                break;
            case MAGIC_TAB:
                color = config.magicTabColor();
                break;
            case SAILING_TAB:
                color = config.sailingTabColor();
                break;
            case CLOSE_CARGO:
                color = config.cargoCloseColor();
                break;
            case ALCH_ITEM:
                color = config.alchemy();
                break;
            default:
                color = config.current();
                break;
        }
        if (current != null) draw(g, current, color);
        return null;
    }

    private void draw(Graphics2D g, Shape shape, Color color) {
        Color old = g.getColor();
        Stroke stroke = g.getStroke();
        g.setColor(
                new Color(
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        config.fill() * 255 / 100));
        g.fill(shape);
        g.setStroke(new BasicStroke(2));
        g.setColor(color);
        g.draw(shape);
        g.setStroke(stroke);
        g.setColor(old);
    }
}
