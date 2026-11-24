package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.HudModule;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HudEditor extends Screen {
    private HudModule dragging;
    private int offX, offY;

    public HudEditor() {
        super(Component.literal("HUD Editor"));
    }

    private List<HudModule> huds() {
        List<HudModule> list = new ArrayList<>();
        for (Module m : ModuleManager.all()) {
            if (m instanceof HudModule h && h.isEnabled()) list.add(h);
        }
        return list;
    }

    private boolean inside(HudModule h, double mx, double my) {
        return mx >= h.x && mx <= h.x + h.width() && my >= h.y && my <= h.y + h.height();
    }

    private void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x - 1, y - 1, x + w + 1, y, c);
        g.fill(x - 1, y + h, x + w + 1, y + h + 1, c);
        g.fill(x - 1, y, x, y + h, c);
        g.fill(x + w, y, x + w + 1, y + h, c);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x44000000);
        for (HudModule h : huds()) {
            boolean hover = inside(h, mouseX, mouseY);
            int color = h == dragging ? 0xFFFF7B1D : (hover ? 0xFFFFFFFF : 0x88FFFFFF);
            outline(g, h.x, h.y, h.width(), h.height(), color);
            if (hover || h == dragging) {
                String label = h.name + "  " + Math.round(h.scale.get() * 100) + "%";
                g.drawString(font, label, h.x, Math.max(2, h.y - 11), 0xFFFF7B1D);
            }
        }
        String hint = "ЛКМ — двигать, колёсико — размер, Esc — выйти";
        g.drawString(font, hint, (width - font.width(hint)) / 2, 20, 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            List<HudModule> list = huds();
            for (int i = list.size() - 1; i >= 0; i--) {
                HudModule h = list.get(i);
                if (inside(h, event.x(), event.y())) {
                    dragging = h;
                    offX = (int) event.x() - h.x;
                    offY = (int) event.y() - h.y;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            int nx = (int) event.x() - offX;
            int ny = (int) event.y() - offY;
            dragging.x = Math.max(0, Math.min(width - dragging.width(), nx));
            dragging.y = Math.max(0, Math.min(height - dragging.height(), ny));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<HudModule> list = huds();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule h = list.get(i);
            if (inside(h, mouseX, mouseY)) {
                double v = h.scale.get() + scrollY * 0.1;
                v = Math.max(h.scale.min, Math.min(h.scale.max, v));
                h.scale.set(Math.round(v * 20) / 20.0);
                h.x = Math.max(0, Math.min(width - h.width(), h.x));
                h.y = Math.max(0, Math.min(height - h.height(), h.y));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
