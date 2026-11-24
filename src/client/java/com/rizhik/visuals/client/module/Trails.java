package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;

public class Trails extends Module {
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Double> size = add(new Setting<Double>("Размер", 1.0, 0.4, 2.0));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));

    private double lx, ly, lz;
    private boolean has;

    public Trails() {
        super("Trails", Category.WORLD,
                "Светящийся шлейф за тобой при беге. Цвет, размер частиц и радужный режим настраиваются.");
    }

    @Override
    protected void onDisable() {
        has = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        var p = mc.player;
        if (p == null || mc.level == null) {
            has = false;
            return;
        }
        double x = p.getX(), y = p.getY() + 0.1, z = p.getZ();
        if (has) {
            double dx = x - lx, dy = y - ly, dz = z - lz;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > 0.02) {
                int steps = Math.max(1, (int) (d / 0.15));
                int rgb = rainbow.get() ? Fx.rainbow() : color.rgb();
                for (int i = 0; i < steps; i++) {
                    double t = i / (double) steps;
                    Fx.dust(mc,
                            lx + dx * t + (Math.random() - 0.5) * 0.3,
                            ly + dy * t,
                            lz + dz * t + (Math.random() - 0.5) * 0.3,
                            rgb, size.get().floatValue());
                }
            }
        }
        lx = x;
        ly = y;
        lz = z;
        has = true;
    }
}
