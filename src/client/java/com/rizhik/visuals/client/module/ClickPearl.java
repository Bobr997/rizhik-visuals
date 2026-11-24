package com.rizhik.visuals.client.module;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class ClickPearl extends Module {
    public static KeyMapping key;

    public ClickPearl() {
        super("ClickPearl", Category.PVP,
                "Бросает эндер-перл по клавише (по умолчанию G), даже если перл лежит в любом слоте хотбара или инвентаря, и возвращает прошлый слот. Клавишу можно сменить в настройках управления.");
    }

    @Override
    protected void onEnable() {
        if (key != null) while (key.consumeClick()) { }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (key == null) return;
        boolean pressed = false;
        while (key.consumeClick()) pressed = true;
        if (!pressed || mc.player == null || mc.gameMode == null || mc.screen != null) return;
        throwPearl(mc);
    }

    private void use(Minecraft mc, InteractionHand hand) {
        mc.gameMode.useItem(mc.player, hand);
        mc.player.swing(hand);
    }

    private boolean cooling(Minecraft mc, int slot) {
        var st = mc.player.getInventory().getItem(slot);
        return mc.player.getCooldowns().getCooldownPercent(st, 0f) > 0f;
    }

    private void swap(Minecraft mc, int invSlot, int hotbar) {
        mc.gameMode.handleInventoryMouseClick(
                mc.player.inventoryMenu.containerId, invSlot, hotbar, ClickType.SWAP, mc.player);
    }

    private void throwPearl(Minecraft mc) {
        var inv = mc.player.getInventory();
        int selected = inv.getSelectedSlot();

        if (inv.getItem(selected).is(Items.ENDER_PEARL)) {
            if (!cooling(mc, selected)) use(mc, InteractionHand.MAIN_HAND);
            return;
        }
        if (mc.player.getOffhandItem().is(Items.ENDER_PEARL)) {
            use(mc, InteractionHand.OFF_HAND);
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.ENDER_PEARL)) {
                if (cooling(mc, i)) return;
                inv.setSelectedSlot(i);
                use(mc, InteractionHand.MAIN_HAND);
                inv.setSelectedSlot(selected);
                return;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getItem(i).is(Items.ENDER_PEARL)) {
                if (cooling(mc, i)) return;
                swap(mc, i, selected);
                use(mc, InteractionHand.MAIN_HAND);
                swap(mc, i, selected);
                return;
            }
        }
    }
}
