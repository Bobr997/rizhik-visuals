package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    private final Setting<Double> delay = add(new Setting<Double>("Задержка (тики)", 1.0, 1, 10));
    private final Setting<Boolean> onlyLow = add(new Setting<Boolean>("Только при низком ХП", false));
    private final Setting<Double> hp = add(new Setting<Double>("Порог ХП", 10.0, 2, 20));

    private int cool;

    public AutoTotem() {
        super("AutoTotem", Category.PVP,
                "Сам берёт тотем бессмертия из инвентаря во вторую руку, когда там его нет (например, после срабатывания). Можно включить только при низком здоровье и задать задержку.");
    }

    @Override
    protected void onEnable() {
        cool = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        var p = mc.player;
        if (p == null || mc.gameMode == null || mc.screen != null) {
            cool = 0;
            return;
        }
        if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            cool = 0;
            return;
        }
        if (onlyLow.get() && p.getHealth() + p.getAbsorptionAmount() > hp.get()) return;

        int need = Math.max(1, (int) Math.round(delay.get()));
        if (cool < need) {
            cool++;
            return;
        }

        var inv = p.getInventory();
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;

        int menuSlot = slot < 9 ? slot + 36 : slot;
        mc.gameMode.handleInventoryMouseClick(
                p.inventoryMenu.containerId, menuSlot, 40, ClickType.SWAP, p);
        cool = 0;
    }
}
