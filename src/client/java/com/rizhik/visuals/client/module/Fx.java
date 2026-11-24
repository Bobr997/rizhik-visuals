package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;

public class Fx {
    public static void dust(Minecraft mc, double x, double y, double z, int rgb, float size) {
        if (mc.level == null) return;
        mc.level.addParticle(new DustParticleOptions(rgb & 0xFFFFFF, size), x, y, z, 0, 0, 0);
    }

    public static int rainbow() {
        return ColorSetting.hsv((System.currentTimeMillis() % 4000) / 4000f, 0.85f, 1f);
    }
}
