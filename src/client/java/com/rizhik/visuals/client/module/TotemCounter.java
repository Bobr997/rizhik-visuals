package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class TotemCounter extends HudModule {
    public TotemCounter() {
        super("TotemCounter", "Иконка тотема бессмертия и сколько их у тебя в инвентаре и второй руке.", 6, 140);
    }

    @Override
    protected List<Row> rows() {
        Minecraft mc = Minecraft.getInstance();
        int count = 0;
        if (mc.player != null) {
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack st = inv.getItem(i);
                if (st.is(Items.TOTEM_OF_UNDYING)) count += st.getCount();
            }
        }
        return List.of(new Row(new ItemStack(Items.TOTEM_OF_UNDYING), "x" + count));
    }
}
