package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.HudEditor;
import com.rizhik.visuals.client.gui.Render;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TargetHud extends HudModule {
    private static final int W = 136, H = 66;
    private static final long SHOW_MS = 8000;

    private Player target;
    private long lastHit;

    public TargetHud() {
        super("TargetHUD",
                "Окошко игрока, которого ты ударил последним: ник, ХП, предмет в руке, броня и её прочность.",
                200, 6);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (player == Minecraft.getInstance().player && entity instanceof Player p) {
                target = p;
                lastHit = System.currentTimeMillis();
            }
            return InteractionResult.PASS;
        });
    }

    private Player current() {
        Minecraft mc = Minecraft.getInstance();
        if (target != null && target.isAlive() && !target.isRemoved()
                && System.currentTimeMillis() - lastHit < SHOW_MS) {
            return target;
        }
        if (mc.screen instanceof HudEditor) return mc.player;
        return null;
    }

    @Override
    protected List<Row> rows() {
        return List.of();
    }

    @Override
    public int width() { return W; }

    @Override
    public int height() { return H; }

    @Override
    public void renderHud(GuiGraphics g) {
        Player p = current();
        if (p == null) return;
        Font f = Minecraft.getInstance().font;
        int bg = ((int) (opacity.get() / 100.0 * 255) << 24) | 0x111114;

        Render.roundedRect(g, x, y, W, H, 6, bg);
        Render.roundedRect(g, x + 3, y + 5, 2, H - 10, 1, color.rgb());

        g.drawString(f, f.plainSubstrByWidth(p.getName().getString(), W - 20), x + 10, y + 6, 0xFFFFFFFF);

        float max = Math.max(1f, p.getMaxHealth());
        float ratio = Math.max(0f, Math.min(1f, p.getHealth() / max));
        float total = p.getHealth() + p.getAbsorptionAmount();
        g.drawString(f, String.format("%.1f HP", total), x + 10, y + 18, 0xFFCCCCCC);

        int barX = x + 10, barY = y + 29, barW = W - 20;
        Render.roundedRect(g, barX, barY, barW, 4, 2, 0xFF3A3A42);
        Render.roundedRect(g, barX, barY, Math.max(4, (int) (barW * ratio)), 4, 2,
                ColorSetting.hsv(ratio * 0.33f, 0.85f, 1f));

        EquipmentSlot[] slots = {
                EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (int i = 0; i < slots.length; i++) {
            ItemStack st = p.getItemBySlot(slots[i]);
            int sx = x + 10 + i * 24;
            int sy = y + 37;
            if (st.isEmpty()) {
                Render.roundedRect(g, sx, sy, 16, 16, 3, 0x33FFFFFF);
                continue;
            }
            g.renderItem(st, sx, sy);
            g.renderItemDecorations(f, st, sx, sy);
            if (st.isDamageableItem() && st.getMaxDamage() > 0) {
                int pct = 100 * (st.getMaxDamage() - st.getDamageValue()) / st.getMaxDamage();
                g.drawString(f, pct + "%", sx, sy + 18, 0xFFAAAAAA);
            }
        }
    }
}
