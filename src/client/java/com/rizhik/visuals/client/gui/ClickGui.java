package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.Category;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClickGui extends Screen {
    private static final int PW = 440, PH = 260, SIDE = 100, PAD = 10, CARD_H = 38, HEAD = 34;
    private static final int ORANGE = 0xFFFF7B1D;
    private Category selected = Category.PVP;
    private double scroll = 0;

    public ClickGui() {
        super(Component.literal("Rizhik Visuals"));
    }

    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private int cardW() { return (PW - SIDE - PAD * 3) / 2; }
    private int cardX(int col) { return px() + SIDE + PAD + col * (cardW() + PAD); }
    private int cardY(int row) { return py() + HEAD + PAD + row * (CARD_H + PAD) - (int) scroll; }
    private int sideY(int i) { return py() + 44 + i * 26; }

    private double maxScroll() {
        int n = ModuleManager.byCategory(selected).size();
        int rows = (n + 1) / 2;
        int content = rows * (CARD_H + PAD);
        int view = PH - HEAD - PAD - 6;
        return Math.max(0, content - view);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
        int x = px(), y = py();

        Render.roundedRect(g, x, y, PW, PH, 10, 0xF0111114);
        Render.roundedRect(g, x, y, SIDE, PH, 10, 0xF00C0C0F);

        g.drawString(font, "Rizhik", x + 14, y + 14, ORANGE);
        g.drawString(font, "Visuals", x + 14 + font.width("Rizhik "), y + 14, 0xFFFFFFFF);

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int sy = sideY(i);
            boolean active = cats[i] == selected;
            if (active) Render.roundedRect(g, x + 8, sy, SIDE - 16, 20, 6, ORANGE);
            g.drawString(font, cats[i].title, x + 18, sy + 6, active ? 0xFFFFFFFF : 0xFF8A8A93);
        }

        g.drawString(font, selected.title, x + SIDE + PAD, y + 14, 0xFFFFFFFF);

        g.enableScissor(x + SIDE, y + HEAD, x + PW, y + PH - 4);
        List<Module> mods = ModuleManager.byCategory(selected);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int cx = cardX(i % 2);
            int cy = cardY(i / 2);
            int cw = cardW();
            Render.roundedRect(g, cx, cy, cw, CARD_H, 6, 0xFF1A1A1F);
            g.drawString(font, m.name, cx + 8, cy + 8, 0xFFFFFFFF);
            g.drawString(font, font.plainSubstrByWidth(m.description, cw - 52), cx + 8, cy + 22, 0xFF8A8A93);

            int tx = cx + cw - 32, ty = cy + 12;
            Render.roundedRect(g, tx, ty, 24, 12, 6, m.isEnabled() ? ORANGE : 0xFF3A3A42);
            int kx = m.isEnabled() ? tx + 13 : tx + 1;
            Render.roundedRect(g, kx, ty + 1, 10, 10, 5, 0xFFFFFFFF);
        }
        g.disableScissor();

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x(), my = event.y();
            int x = px(), y = py();

            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int sy = sideY(i);
                if (mx >= x + 8 && mx <= x + SIDE - 8 && my >= sy && my <= sy + 20) {
                    selected = cats[i];
                    scroll = 0;
                    return true;
                }
            }

            if (my >= y + HEAD && my <= y + PH - 4) {
                List<Module> mods = ModuleManager.byCategory(selected);
                for (int i = 0; i < mods.size(); i++) {
                    int cx = cardX(i % 2);
                    int cy = cardY(i / 2);
                    if (mx >= cx && mx <= cx + cardW() && my >= cy && my <= cy + CARD_H) {
                        mods.get(i).toggle();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - scrollY * 14));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
