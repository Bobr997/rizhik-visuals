package com.rizhik.visuals.client.gui;

import net.minecraft.client.gui.GuiGraphics;

public class Render {
    public static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int inset = 0;
            if (i < r) inset = corner(r, r - i);
            else if (i >= h - r) inset = corner(r, i - (h - r) + 1);
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
        }
    }

    private static int corner(int r, int dy) {
        double dx = Math.sqrt(Math.max(0, r * r - (dy - 0.5) * (dy - 0.5)));
        return (int) Math.round(r - dx);
    }
}
