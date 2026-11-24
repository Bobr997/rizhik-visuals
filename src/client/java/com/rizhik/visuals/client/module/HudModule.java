package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public abstract class HudModule extends Module {
    public int x, y;
    protected final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 80.0, 20, 100));
    protected final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));

    protected HudModule(String name, String description, int x, int y) {
        super(name, Category.HUD, description);
        this.x = x;
        this.y = y;
    }

    protected abstract List<String> lines();

    public int width() {
        Font f = Minecraft.getInstance().font;
        int w = 0;
        for (String s : lines()) w = Math.max(w, f.width(s));
        return w + 14;
    }

    public int height() {
        return Math.max(1, lines().size()) * 11 + 7;
    }

    @Override
    public void renderHud(GuiGraphics g) {
        List<String> ls = lines();
        if (ls.isEmpty()) return;
        Font f = Minecraft.getInstance().font;
        int bg = ((int) (opacity.get() / 100.0 * 255) << 24) | 0x111114;
        int w = width(), h = height();

        Render.roundedRect(g, x, y, w, h, 5, bg);
        Render.roundedRect(g, x + 3, y + 5, 2, h - 10, 1, color.rgb());
        for (int i = 0; i < ls.size(); i++) {
            g.drawString(f, ls.get(i), x + 9, y + 5 + i * 11, 0xFFFFFFFF);
        }
    }
}
