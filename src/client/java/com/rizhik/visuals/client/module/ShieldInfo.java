package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ShieldInfo extends HudModule {
    public ShieldInfo() {
        super("ShieldInfo", "Показывает щит, сколько в нём прочности и стоит ли он на перезарядке.", 6, 175);
    }

    @Override
    protected List<Row> rows() {
        Minecraft mc = Minecraft.getInstance();
        ItemStack shield = ItemStack.EMPTY;
        if (mc.player != null) {
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack st = inv.getItem(i);
                if (st.is(Items.SHIELD)) {
                    shield = st;
                    break;
                }
            }
        }
        if (shield.isEmpty()) {
            return List.of(new Row(new ItemStack(Items.SHIELD), "нет"));
        }
        String text = durability(shield);
        if (mc.player.getCooldowns().getCooldownPercent(shield, 0f) > 0f) text += "  кд";
        return List.of(new Row(shield, text.isEmpty() ? "ок" : text));
    }
}
