package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class Watermark extends Module {
    public Watermark() {
        super("Watermark", Category.HUD, "Плашка с названием клиента и FPS");
    }

    @Override
    public void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        String text = "Rizhik Visuals | " + mc.getFps() + " FPS";
        int w = mc.font.width(text) + 14;
        Render.roundedRect(g, 6, 6, w, 18, 5, 0xCC111114);
        Render.roundedRect(g, 9, 11, 2, 8, 1, 0xFFFF7B1D);
        g.drawString(mc.font, text, 15, 11, 0xFFFFFFFF);
    }
}
