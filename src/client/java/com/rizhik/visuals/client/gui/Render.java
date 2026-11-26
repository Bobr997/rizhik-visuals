package com.rizhik.visuals.client.gui;

import net.minecraft.client.gui.GuiGraphics;

public class Render {
    public static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        Ui.rrect(g, x, y, w, h, r, color);
    }
}
