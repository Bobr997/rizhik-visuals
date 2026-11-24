package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class HudModule extends Module {
    public record Row(ItemStack icon, String text) {}

    public int x, y;
    protected final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 80.0, 20, 100));
    protected final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));

    protected HudModule(String name, String description, int x, int y) {
        super(name, Category.HUD, description);
        this.x = x;
        this.y = y;
    }

    protected abstract List<Row> rows();

    private static int rowHeight(List<Row> rs) {
        for (Row r : rs) if (r.icon() != null) return 18;
        return 11;
    }

    public int width() {
        Font f = Minecraft.getInstance().font;
        int w = 0;
        for (Row r : rows()) {
            w = Math.max(w, f.width(r.text()) + (r.icon() != null ? 20 : 0));
        }
        return w + 16;
    }

    public int height() {
        List<Row> rs = rows();
        return Math.max(1, rs.size()) * rowHeight(rs) + 8;
    }

    @Override
    public void renderHud(GuiGraphics g) {
        List<Row> rs = rows();
        if (rs.isEmpty()) return;
        Font f = Minecraft.getInstance().font;
        int bg = ((int) (opacity.get() / 100.0 * 255) << 24) | 0x111114;
        int w = width(), h = height(), rh = rowHeight(rs);

        Render.roundedRect(g, x, y, w, h, 5, bg);
        Render.roundedRect(g, x + 3, y + 5, 2, h - 10, 1, color.rgb());
        for (int i = 0; i < rs.size(); i++) {
            Row r = rs.get(i);
            int ry = y + 4 + i * rh;
            int tx = x + 9;
            if (r.icon() != null) {
                g.renderItem(r.icon(), tx, ry + 1);
                tx += 20;
            }
            g.drawString(f, r.text(), tx, ry + (rh - 9) / 2, 0xFFFFFFFF);
        }
    }
}
