package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Trails extends Module {
    private final ModeSetting mode = add(new ModeSetting("Вид", 0, "Лента", "Стена"));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> width = add(new Setting<Double>("Ширина", 0.35, 0.1, 0.9));
    private final Setting<Double> life = add(new Setting<Double>("Длина", 1.2, 0.3, 3.0));

    private final List<double[]> pts = new ArrayList<>();

    public Trails() {
        super("Trails", Category.WORLD,
                "Светящийся след за тобой: лента по земле или светящаяся стена. Вид, цвет, ширина, длина и радужный режим настраиваются.");
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
        double lifeMs = life.get() * 1000.0;
        pts.removeIf(pt -> now - pt[3] > lifeMs);
        if (pts.isEmpty()) return;

        List<double[]> list = new ArrayList<>(pts);
        list.add(new double[]{p.getX(), p.getY(), p.getZ(), now});
        boolean wall = mode.get() == 1;
        double half = width.get() / 2.0;

        for (int i = 0; i < list.size() - 1; i++) {
            double[] a = list.get(i), b = list.get(i + 1);
            double dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1e-4 || len > 4 || Math.abs(dy) > 4) continue;

            double aa = Math.max(0, 1 - (now - a[3]) / lifeMs);
            double ab = Math.max(0, 1 - (now - b[3]) / lifeMs);
            int ca = WorldFx.argb(colorAt(a[3]), aa);
            int cb = WorldFx.argb(colorAt(b[3]), ab);

            if (wall) {
                double h = 0.9;
                int ta = WorldFx.argb(colorAt(a[3]), 0);
                int tb = WorldFx.argb(colorAt(b[3]), 0);
                q.vertex(a[0], a[1], a[2], ca);
                q.vertex(b[0], b[1], b[2], cb);
                q.vertex(b[0], b[1] + h, b[2], tb);
                q.vertex(a[0], a[1] + h, a[2], ta);
            } else {
                double nx = -dz / len, nz = dx / len;
                double wa = half * aa, wb = half * ab;
                double ya = a[1] + 0.03, yb = b[1] + 0.03;
                q.vertex(a[0] + nx * wa, ya, a[2] + nz * wa, ca);
                q.vertex(a[0] - nx * wa, ya, a[2] - nz * wa, ca);
                q.vertex(b[0] - nx * wb, yb, b[2] - nz * wb, cb);
                q.vertex(b[0] + nx * wb, yb, b[2] + nz * wb, cb);
            }
        }
    }
}
