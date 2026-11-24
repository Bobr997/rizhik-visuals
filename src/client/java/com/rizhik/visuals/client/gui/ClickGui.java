package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.Category;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGui extends Screen {
    private static final int COL_W = 110, HEADER_H = 18, ROW_H = 16, PAD = 10, TOP = 20;

    public ClickGui() {
        super(Component.literal("Rizhik Visuals"));
    }

    private int colX(int i) {
        return PAD + i * (COL_W + 6);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x80000000);
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int x = colX(i);
            int y = TOP;
            g.fill(x, y, x + COL_W, y + HEADER_H, 0xFFFF7B1D);
            g.drawString(font, cats[i].title, x + 4, y + 5, 0xFFFFFFFF);
            y += HEADER_H;
            for (Module m : ModuleManager.byCategory(cats[i])) {
                g.fill(x, y, x + COL_W, y + ROW_H, m.isEnabled() ? 0xFF3A2A1A : 0xFF1E1E1E);
                g.drawString(font, m.name, x + 4, y + 4, m.isEnabled() ? 0xFFFF7B1D : 0xFFCCCCCC);
                y += ROW_H;
            }
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int x = colX(i);
            int y = TOP + HEADER_H;
            for (Module m : ModuleManager.byCategory(cats[i])) {
                if (mx >= x && mx <= x + COL_W && my >= y && my <= y + ROW_H) {
                    m.toggle();
                    return true;
                }
                y += ROW_H;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
