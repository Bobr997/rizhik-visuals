package com.rizhik.visuals.client.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomHand extends Module {
    private static CustomHand instance;

    public static class Tweak {
        public final Setting<Double> scale, x, y, z;

        Tweak(String prefix) {
            scale = new Setting<Double>(prefix + ": размер", 1.0, 0.3, 2.0);
            x = new Setting<Double>(prefix + ": X", 0.0, -1.5, 1.5);
            y = new Setting<Double>(prefix + ": Y", 0.0, -1.5, 1.5);
            z = new Setting<Double>(prefix + ": Z", 0.0, -1.5, 1.5);
        }

        public void reset() {
            scale.set(1.0);
            x.set(0.0);
            y.set(0.0);
            z.set(0.0);
        }
    }

    public final Tweak sword = new Tweak("Меч");
    public final Tweak shield = new Tweak("Щит");
    public final Tweak totem = new Tweak("Тотем");

    public CustomHand() {
        super("CustomHand", Category.VISUALS,
                "Меняет размер и положение меча, щита и тотема в руке. Открой редактор клавишей H, зажми Ctrl: мышь двигает предмет, колёсико меняет размер.");
        instance = this;
        for (Tweak t : new Tweak[]{sword, shield, totem}) {
            addHidden(t.scale);
            addHidden(t.x);
            addHidden(t.y);
            addHidden(t.z);
        }
    }

    public static Tweak tweakFor(ItemStack stack) {
        CustomHand h = instance;
        if (h == null) return null;
        if (stack.is(ItemTags.SWORDS)) return h.sword;
        if (stack.is(Items.SHIELD)) return h.shield;
        if (stack.is(Items.TOTEM_OF_UNDYING)) return h.totem;
        return null;
    }

    public static String labelFor(ItemStack stack) {
        if (stack.is(ItemTags.SWORDS)) return "Меч";
        if (stack.is(Items.SHIELD)) return "Щит";
        if (stack.is(Items.TOTEM_OF_UNDYING)) return "Тотем";
        return "";
    }

    public static void transform(PoseStack pose, ItemStack stack) {
        CustomHand h = instance;
        if (h == null || !h.isEnabled()) return;
        Tweak t = tweakFor(stack);
        if (t == null) return;
        pose.translate(t.x.get().floatValue(), t.y.get().floatValue(), t.z.get().floatValue());
        float sc = t.scale.get().floatValue();
        pose.scale(sc, sc, sc);
    }
}
