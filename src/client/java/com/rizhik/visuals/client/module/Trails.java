package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Trails extends Module {
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> height = add(new Setting<Double>("Высота", 1.0, 0.3, 2.5));
    private final Setting<Double> length = add(new Setting<Double>("Длина следа", 3.0, 0.5, 8.0));
    private final Setting<Double> brightness = add(new Setting<Double>("Яркость", 100.0, 30, 100));

    private final List<double[]> pts = new ArrayList<>();

    public Trails() {
        super("Trails", Category.WORLD,
                "Светящаяся стена-след за тобой, видна с обеих сторон. Цвет, высота, длина, яркость и радужный режим настраиваются.");
    }

    @Override
    protected void onDisable() {
        pts.clear();
    }

    @Override
    public void onTick(Minecraft mc) {
        var p = mc.player;
        if (p == null) {
            pts.clear();
            return;
        }
        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (!pts.isEmpty()) {
            double[] l = pts.get(pts.size() - 1);
            double dx = x - l[0], dy = y - l[1], dz = z - l[2];
            if (dx * dx + dy * dy + dz * dz < 0.0064) return;
        }
        pts.add(new double[]{x, y, z, System.currentTimeMillis()});
    }

    private int colorAt(double t) {
        return rainbow.get() ? ColorSetting.hsv(((long) t % 3000L) / 3000f, 0.85f, 1f) : color.rgb();
    }

    @Override
    public void renderWorld(WorldFx.Quads q) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;

        long now = System.currentTimeMillis();
        double lifeMs = length.get() * 1000.0;
        pts.removeIf(pt -> now - pt[3] > lifeMs);
        if (pts.isEmpty()) return;

        List<double[]> list = new ArrayList<>(pts);
        list.add(new double[]{p.getX(), p.getY(), p.getZ(), now});
        double h = height.get();
        double bright = brightness.get() / 100.0;

        for (int i = 0; i < list.size() - 1; i++) {
            double[] a = list.get(i), b = list.get(i + 1);
            double dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1e-4 || len > 4 || Math.abs(dy) > 4) continue;

            double fa = Math.max(0, 1 - (now - a[3]) / lifeMs);
            double fb = Math.max(0, 1 - (now - b[3]) / lifeMs);
            double aa = Math.pow(fa, 0.6) * bright;
            double ab = Math.pow(fb, 0.6) * bright;
            int ca = WorldFx.argb(colorAt(a[3]), aa);
            int cb = WorldFx.argb(colorAt(b[3]), ab);
            int ta = WorldFx.argb(colorAt(a[3]), aa * 0.35);
            int tb = WorldFx.argb(colorAt(b[3]), ab * 0.35);

            q.vertex(a[0], a[1], a[2], ca);
            q.vertex(b[0], b[1], b[2], cb);
            q.vertex(b[0], b[1] + h, b[2], tb);
            q.vertex(a[0], a[1] + h, a[2], ta);
        }
    }
}
