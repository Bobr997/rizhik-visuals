package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.HudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Cooldowns extends HudModule {
    private final Map<Item, double[]> info = new HashMap<>();
    private final List<Row> rows = new ArrayList<>();

    public Cooldowns() {
        super("Cooldowns",
                "Показывает все предметы, которые сейчас на перезарядке (перлы, яблоки, хорусы и др.), с таймером.",
                6, 40);
    }

    @Override
    public void onTick(Minecraft mc) {
        rows.clear();
        if (mc.player == null) {
            info.clear();
            return;
        }
        var cd = mc.player.getCooldowns();
        var inv = mc.player.getInventory();
        Set<Item> seen = new HashSet<>();
        Set<Item> active = new HashSet<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty() || !seen.add(st.getItem())) continue;
            float p = cd.getCooldownPercent(st, 0f);
            if (p <= 0f) continue;

            active.add(st.getItem());
            String name = st.getHoverName().getString();
            double[] d = info.get(st.getItem());
            if (d == null) {
                info.put(st.getItem(), new double[]{p, 0});
                rows.add(new Row(st.copyWithCount(1), name + "  ..."));
                continue;
            }
            double delta = d[0] - p;
            if (d[1] == 0 && delta > 0.0001) d[1] = 1.0 / delta;
            d[0] = p;
            String time = d[1] > 0 ? String.format("%.1f с", p * d[1] / 20.0) : "...";
            rows.add(new Row(st.copyWithCount(1), name + "  " + time));
        }
        info.keySet().retainAll(active);
    }

    @Override
    protected List<Row> rows() {
        if (rows.isEmpty() && Minecraft.getInstance().screen instanceof HudEditor) {
            return List.of(new Row(new ItemStack(Items.ENDER_PEARL), "Эндер-перл  4.2 с"));
        }
        return rows;
    }
}
