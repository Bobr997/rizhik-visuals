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
    protected boolean visible() {
        return current() != null;
    }

    @Override
    protected int baseWidth() { return W; }

    @Override
    protected int baseHeight() { return H; }

    @Override
    protected void draw(GuiGraphics g) {
        Player p = current();
        if (p == null) return;
        Font f = Minecraft.getInstance().font;

        Render.roundedRect(g, 0, 0, W, H, 6, bg());
        Render.roundedRect(g, 3, 5, 2, H - 10, 1, color.rgb());

        g.drawString(f, f.plainSubstrByWidth(p.getName().getString(), W - 20), 10, 6, 0xFFFFFFFF);

        float max = Math.max(1f, p.getMaxHealth());
        float ratio = Math.max(0f, Math.min(1f, p.getHealth() / max));
        float total = p.getHealth() + p.getAbsorptionAmount();
        g.drawString(f, String.format("%.1f HP", total), 10, 18, 0xFFCCCCCC);

        int barX = 10, barY = 29, barW = W - 20;
        Render.roundedRect(g, barX, barY, barW, 4, 2, 0xFF3A3A42);
        Render.roundedRect(g, barX, barY, Math.max(4, (int) (barW * ratio)), 4, 2,
                ColorSetting.hsv(ratio * 0.33f, 0.85f, 1f));

        EquipmentSlot[] slots = {
                EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (int i = 0; i < slots.length; i++) {
            ItemStack st = p.getItemBySlot(slots[i]);
            int sx = 10 + i * 24;
            int sy = 37;
            if (st.isEmpty()) {
                Render.roundedRect(g, sx, sy, 16, 16, 3, 0x33FFFFFF);
                continue;
            }
            g.renderItem(st, sx, sy);
            g.renderItemDecorations(f, st, sx, sy);
            String d = durability(st);
            if (!d.isEmpty()) g.drawString(f, d, sx, sy + 18, 0xFFAAAAAA);
        }
    }
}
