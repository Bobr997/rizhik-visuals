package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class Watermark extends Module {
    private final Setting<Boolean> showFps = add(new Setting<Boolean>("Показывать FPS", true));
    private final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 80.0, 20, 100));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));

    public Watermark() {
        super("Watermark", Category.HUD,
                "Плашка с названием клиента и FPS в углу экрана. Показывает, что клиент работает.");
    }

    @Override
    public void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        String text = showFps.get()
                ? "Rizhik Visuals | " + mc.getFps() + " FPS"
                : "Rizhik Visuals";
        int w = mc.font.width(text) + 14;
        int alpha = (int) (opacity.get() / 100.0 * 255);
        int bg = (alpha << 24) | 0x111114;

        Render.roundedRect(g, 6, 6, w, 18, 5, bg);
        Render.roundedRect(g, 9, 11, 2, 8, 1, color.rgb());
        g.drawString(mc.font, text, 15, 11, 0xFFFFFFFF);
    }
}
