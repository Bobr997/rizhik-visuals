package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class Keystrokes extends HudModule {
    public Keystrokes() {
        super("Keystrokes", "Показывает клавиши движения и прыжка (WASD и пробел), нажатые подсвечиваются.", 6, 210);
    }

    @Override
    protected List<Row> rows() {
        return List.of();
    }

    @Override
    protected boolean visible() {
        return true;
    }

    @Override
    protected int baseWidth() { return 66; }

    @Override
    protected int baseHeight() { return 60; }

    private void key(GuiGraphics g, KeyMapping k, String label, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        Render.roundedRect(g, x, y, w, h, 4, k.isDown() ? color.rgb() : bg());
        g.drawString(f, label, x + (w - f.width(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    protected void draw(GuiGraphics g) {
        var o = Minecraft.getInstance().options;
        key(g, o.keyUp, "W", 23, 0, 20, 20);
        key(g, o.keyLeft, "A", 0, 23, 20, 20);
        key(g, o.keyDown, "S", 23, 23, 20, 20);
        key(g, o.keyRight, "D", 46, 23, 20, 20);
        key(g, o.keyJump, "—", 0, 46, 66, 14);
    }
}
