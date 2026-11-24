package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircles extends Module {
    private static final int MAX_AGE = 12;

    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Double> radius = add(new Setting<Double>("Радиус", 3.0, 1.0, 6.0));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));

    private final List<double[]> rings = new ArrayList<>();
    private boolean wasGround = true;
    private double groundY;

    public JumpCircles() {
        super("JumpCircles", Category.WORLD,
                "При прыжке от земли расходится светящееся кольцо. Цвет и радиус настраиваются.");
    }

    @Override
    protected void onDisable() {
        rings.clear();
    }

    @Override
    public void onTick(Minecraft mc) {
        var p = mc.player;
        if (p == null || mc.level == null) {
            rings.clear();
            return;
        }
        boolean ground = p.onGround();
        if (ground) {
            groundY = p.getY();
        } else if (wasGround && p.getDeltaMovement().y > 0.1) {
            rings.add(new double[]{p.getX(), groundY, p.getZ(), 0});
        }
        wasGround = ground;

        Iterator<double[]> it = rings.iterator();
        while (it.hasNext()) {
            double[] r = it.next();
            double rad = 0.3 + (radius.get() - 0.3) * (r[3] / MAX_AGE);
            int n = Math.max(12, (int) (rad * 9));
            int rgb = rainbow.get() ? Fx.rainbow() : color.rgb();
            for (int i = 0; i < n; i++) {
                double a = Math.PI * 2 * i / n;
                Fx.dust(mc, r[0] + Math.cos(a) * rad, r[1] + 0.05, r[2] + Math.sin(a) * rad, rgb, 0.6f);
            }
            r[3]++;
            if (r[3] > MAX_AGE) it.remove();
        }
    }
}
