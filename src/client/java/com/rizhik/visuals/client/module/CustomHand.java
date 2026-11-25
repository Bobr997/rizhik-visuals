package com.rizhik.visuals.client.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomHand extends Module {
    private static CustomHand instance;

    private final Setting<Double> swordScale = add(new Setting<Double>("Меч: размер", 1.0, 0.3, 1.6));
    private final Setting<Double> swordX = add(new Setting<Double>("Меч: X", 0.0, -0.6, 0.6));
    private final Setting<Double> swordY = add(new Setting<Double>("Меч: Y", 0.0, -0.6, 0.6));
    private final Setting<Double> swordZ = add(new Setting<Double>("Меч: Z", 0.0, -0.6, 0.6));

    private final Setting<Double> shieldScale = add(new Setting<Double>("Щит: размер", 1.0, 0.3, 1.6));
    private final Setting<Double> shieldX = add(new Setting<Double>("Щит: X", 0.0, -0.6, 0.6));
    private final Setting<Double> shieldY = add(new Setting<Double>("Щит: Y", 0.0, -0.6, 0.6));
    private final Setting<Double> shieldZ = add(new Setting<Double>("Щит: Z", 0.0, -0.6, 0.6));

    private final Setting<Double> totemScale = add(new Setting<Double>("Тотем: размер", 1.0, 0.3, 1.6));
    private final Setting<Double> totemX = add(new Setting<Double>("Тотем: X", 0.0, -0.6, 0.6));
    private final Setting<Double> totemY = add(new Setting<Double>("Тотем: Y", 0.0, -0.6, 0.6));
    private final Setting<Double> totemZ = add(new Setting<Double>("Тотем: Z", 0.0, -0.6, 0.6));

    public CustomHand() {
        super("CustomHand", Category.VISUALS,
                "Меняет размер и положение меча, щита и тотема в руке от первого лица. X двигает вправо, Y вверх, Z вперёд. Меняется вся рука с предметом.");
        instance = this;
    }

    public static void transform(PoseStack pose, ItemStack stack) {
        CustomHand h = instance;
        if (h == null || !h.isEnabled()) return;
        if (stack.is(ItemTags.SWORDS)) {
            h.apply(pose, h.swordScale, h.swordX, h.swordY, h.swordZ);
        } else if (stack.is(Items.SHIELD)) {
            h.apply(pose, h.shieldScale, h.shieldX, h.shieldY, h.shieldZ);
        } else if (stack.is(Items.TOTEM_OF_UNDYING)) {
            h.apply(pose, h.totemScale, h.totemX, h.totemY, h.totemZ);
        }
    }

    private void apply(PoseStack pose, Setting<Double> s, Setting<Double> x, Setting<Double> y, Setting<Double> z) {
        pose.translate(x.get().floatValue(), y.get().floatValue(), z.get().floatValue());
        float sc = s.get().floatValue();
        pose.scale(sc, sc, sc);
    }
}
