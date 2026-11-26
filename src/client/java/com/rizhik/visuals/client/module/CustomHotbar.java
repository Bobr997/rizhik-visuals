package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Ui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class CustomHotbar extends Module {
    public static CustomHotbar INSTANCE;

    private final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 85.0, 30, 100));
    private final Setting<Double> radius = add(new Setting<Double>("Скругление", 7.0, 0, 10));
    private final ColorSetting accent = add(new ColorSetting("Цвет", 0.74f));
    private final Setting<Boolean> counts = add(new Setting<Boolean>("Количество", true));
    private final Setting<Boolean> bars = add(new Setting<Boolean>("Прочность", true));

    private float selX = -1f;
    private long last = System.nanoTime();

    public CustomHotbar() {
        super("Hotbar", Category.HUD,
                "Свой хотбар: тёмная скруглённая панель, плавно скользящая подсветка слота, количество предметов и полоска прочности. Цвет, прозрачность и скругление настраиваются.");
        INSTANCE = this;
    }

    private void item(GuiGraphics g, Minecraft mc, ItemStack st, int ix, int iy) {
        g.renderItem(st, ix, iy);
        if (bars.get() && st.isBarVisible()) {
            Ui.rrect(g, ix + 1.5, iy + 13, 13, 2, 1, 0xFF000000);
            Ui.rrect(g, ix + 1.5, iy + 13, Math.max(1, st.getBarWidth()), 2, 1, 0xFF000000 | st.getBarColor());
        }
        if (counts.get() && st.getCount() > 1) {
            String t = String.valueOf(st.getCount());
            g.drawString(mc.font, t, ix + 17 - mc.font.width(t), iy + 9, 0xFFFFFFFF, true);
        }
    }

    public void draw(GuiGraphics g, DeltaTracker tracker) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || p.isSpectator()) return;
        var inv = p.getInventory();

        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - last) / 1e9f);
        last = now;

        int bw = 182, bh = 22;
        int x = (g.guiWidth() - bw) / 2;
        int y = g.guiHeight() - bh;
        int acc = accent.rgb();
        double op = opacity.get() / 100.0;
        double r = radius.get();

        Ui.rrect(g, x, y, bw, bh, r, Ui.alpha(Ui.BG, op));

        int sel = inv.getSelectedSlot();
        float target = x + 2 + sel * 20;
        selX = selX < 0 ? target : Ui.approach(selX, target, dt, 22f);
        double sr = Math.min(5, r);
        Ui.rrect(g, selX, y + 2, 18, 18, sr, acc);
        double b = Ui.px(1.5);
        Ui.rrect(g, selX + b, y + 2 + b, 18 - 2 * b, 18 - 2 * b, Math.max(0, sr - b), Ui.mix(Ui.BG, acc, 0.28));

        for (int i = 0; i < 9; i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty()) continue;
            item(g, mc, st, x + 2 + i * 20 + 1, y + 3);
        }

        ItemStack off = p.getOffhandItem();
        if (!off.isEmpty()) {
            boolean right = p.getMainArm() == HumanoidArm.RIGHT;
            int ox = right ? x - 26 : x + bw + 4;
            Ui.rrect(g, ox, y, 22, 22, r, Ui.alpha(Ui.BG, op));
            item(g, mc, off, ox + 3, y + 3);
        }
    }
}
