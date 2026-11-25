package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class JumpCircles extends Module {
    private static final int SEGMENTS = 48;

    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> radius = add(new Setting<Double>("Радиус", 3.0, 1.0, 6.0));
    private final Setting<Double> time = add(new Setting<Double>("Время", 0.9, 0.4, 2.0));

    private final List<double[]> rings = new ArrayList<>();
    private boolean wasGround = true;
    private double groundY;

    public JumpCircles() {
        super("JumpCircles", Category.WORLD,
                "При прыжке от земли расходится светящееся кольцо. Цвет, радиус, скорость и радужный режим настраиваются.");
    }

    @Override
    protected void onDisable() {
        rings.clear();
    }

    @Override
    public void onTick(Minecraft mc) {
        var p = mc.player;
        if (p == null) {
            rings.clear();
            return;
        }
        boolean ground = p.onGround();
        if (ground) {
            groundY = p.getY();
        } else if (wasGround && p.getDeltaMovement().y > 0.1) {
            rings.add(new double[]{p.getX(), groundY, p.getZ(), System.currentTimeMillis()});
        }
        wasGround = ground;
    }

    @Override
    public void renderWorld(WorldFx.Quads q) {
        long now = System.currentTimeMillis();
        double lifeMs = time.get() * 1000.0;
        rings.removeIf(r -> now - r[3] > lifeMs);

        for (double[] r : rings) {
            double t = (now - r[3]) / lifeMs;
            double e = 1 - (1 - t) * (1 - t);
            double rad = 0.3 + (radius.get() - 0.3) * e;
            double w = 0.25 + rad * 0.12;
            double ri = Math.max(0.05, rad - w);
            double y = r[1] + 0.03;

            int rgb = rainbow.get()
                    ? ColorSetting.hsv(((long) r[3] % 3000L) / 3000f, 0.85f, 1f)
                    : color.rgb();
            int outer = WorldFx.argb(rgb, 1 - t);
            int inner = WorldFx.argb(rgb, (1 - t) * 0.1);

            for (int i = 0; i < SEGMENTS; i++) {
                double a0 = Math.PI * 2 * i / SEGMENTS;
                double a1 = Math.PI * 2 * (i + 1) / SEGMENTS;
                q.vertex(r[0] + Math.cos(a0) * rad, y, r[2] + Math.sin(a0) * rad, outer);
                q.vertex(r[0] + Math.cos(a1) * rad, y, r[2] + Math.sin(a1) * rad, outer);
                q.vertex(r[0] + Math.cos(a1) * ri, y, r[2] + Math.sin(a1) * ri, inner);
                q.vertex(r[0] + Math.cos(a0) * ri, y, r[2] + Math.sin(a0) * ri, inner);
            }
        }
    }
}
