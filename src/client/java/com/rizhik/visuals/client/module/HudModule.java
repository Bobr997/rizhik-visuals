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
    public final Setting<Double> scale = add(new Setting<Double>("Размер", 1.0, 0.5, 2.5));
    protected final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 80.0, 20, 100));
    protected final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));

    protected HudModule(String name, String description, int x, int y) {
        super(name, Category.HUD, description);
        this.x = x;
        this.y = y;
    }

    protected abstract List<Row> rows();

    protected boolean visible() {
        return !rows().isEmpty();
    }

    protected int bg() {
        return ((int) (opacity.get() / 100.0 * 255) << 24) | 0x111114;
    }

    protected static String durability(ItemStack st) {
        if (st.isDamageableItem() && st.getMaxDamage() > 0) {
            return (100 * (st.getMaxDamage() - st.getDamageValue()) / st.getMaxDamage()) + "%";
        }
        return "";
    }

    private static int rowHeight(List<Row> rs) {
        for (Row r : rs) if (r.icon() != null) return 18;
        return 11;
    }

    protected int baseWidth() {
        Font f = Minecraft.getInstance().font;
        int w = 0;
        for (Row r : rows()) {
            w = Math.max(w, f.width(r.text()) + (r.icon() != null ? 20 : 0));
        }
        return w + 16;
    }

    protected int baseHeight() {
        List<Row> rs = rows();
        return Math.max(1, rs.size()) * rowHeight(rs) + 8;
    }

    public int width() {
        return Math.round(baseWidth() * scale.get().floatValue());
    }

    public int height() {
        return Math.round(baseHeight() * scale.get().floatValue());
    }

    protected void draw(GuiGraphics g) {
        List<Row> rs = rows();
        Font f = Minecraft.getInstance().font;
        int w = baseWidth(), h = baseHeight(), rh = rowHeight(rs);

        Render.roundedRect(g, 0, 0, w, h, 5, bg());
        Render.roundedRect(g, 3, 5, 2, h - 10, 1, color.rgb());
        for (int i = 0; i < rs.size(); i++) {
            Row r = rs.get(i);
            int ry = 4 + i * rh;
            int tx = 9;
            if (r.icon() != null) {
                g.renderItem(r.icon(), tx, ry + 1);
                tx += 20;
            }
            g.drawString(f, r.text(), tx, ry + (rh - 9) / 2, 0xFFFFFFFF);
        }
    }

    @Override
    public void renderHud(GuiGraphics g) {
        if (!visible()) return;
        float s = scale.get().floatValue();
        g.pose().pushMatrix();
        g.pose().translate((float) x, (float) y);
        g.pose().scale(s, s);
        draw(g);
        g.pose().popMatrix();
    }
}
