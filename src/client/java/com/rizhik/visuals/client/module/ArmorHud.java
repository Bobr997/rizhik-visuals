package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.HudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ArmorHud extends HudModule {
    public ArmorHud() {
        super("ArmorHUD", "Показывает надетую броню и сколько прочности в ней осталось.", 6, 90);
    }

    @Override
    protected List<Row> rows() {
        Minecraft mc = Minecraft.getInstance();
        List<Row> out = new ArrayList<>();
        if (mc.player != null) {
            EquipmentSlot[] slots = {
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            };
            for (EquipmentSlot s : slots) {
                ItemStack st = mc.player.getItemBySlot(s);
                if (st.isEmpty()) continue;
                String d = durability(st);
                out.add(new Row(st, d.isEmpty() ? "-" : d));
            }
        }
        if (out.isEmpty() && mc.screen instanceof HudEditor) {
            out.add(new Row(new ItemStack(Items.DIAMOND_HELMET), "100%"));
            out.add(new Row(new ItemStack(Items.DIAMOND_CHESTPLATE), "100%"));
        }
        return out;
    }
}
