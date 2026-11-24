package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", Category.VISUALS,
                "Выкручивает яркость на максимум, чтобы в пещерах и ночью было светло.");
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
