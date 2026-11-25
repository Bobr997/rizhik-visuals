package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.minecraft.client.Minecraft;

public class ChinaHat extends Module {
    private static final int N = 48;

    private final ModeSetting style = add(new ModeSetting("Стиль", 0, "Градиент", "Сплошной"));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> size = add(new Setting<Double>("Размер", 1.0, 0.6, 1.8));
    private final Setting<Double> lift = add(new Setting<Double>("Высота", 0.12, -0.3, 0.8));
    private final Setting<Double> opacity = add(new Setting<Double>("Прозрачность", 70.0, 20, 100));
    private final Setting<Boolean> firstPerson = add(new Setting<Boolean>("От 1-го лица", false));

    public ChinaHat() {
        super("ChinaHat", Category.VISUALS,
                "Светящаяся конусная шляпа над головой с яркой кромкой. Видна в виде от третьего лица (F5). Стиль, цвет, радужный режим, размер, высота и прозрачность настраиваются.");
    }

    private int rim(int i, long now) {
        if (!rainbow.get()) return color.rgb();
        float hue = ((float) i / N + (now % 4000L) / 4000f) % 1f;
        return ColorSetting.hsv(hue, 0.85f, 1f);
    }

    @Override
    public void renderWorld(WorldFx.Quads q) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null) return;
        if (mc.options.getCameraType().isFirstPerson() && !firstPerson.get()) return;

        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double cx = p.xo + (p.getX() - p.xo) * pt;
        double cy = p.yo + (p.getY() - p.yo) * pt;
        double cz = p.zo + (p.getZ() - p.zo) * pt;

        double top = cy + p.getBbHeight() + lift.get();
        double r = 0.55 * size.get();
        double apexY = top + 0.32 * size.get();
        double op = opacity.get() / 100.0;
        double apexAlpha = style.get() == 1 ? op : op * 0.15;
        long now = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            double a0 = Math.PI * 2 * i / N;
            double a1 = Math.PI * 2 * (i + 1) / N;
            int c0 = rim(i, now);
            int c1 = rim(i + 1, now);

            double x0 = cx + Math.cos(a0) * r, z0 = cz + Math.sin(a0) * r;
            double x1 = cx + Math.cos(a1) * r, z1 = cz + Math.sin(a1) * r;
            int apex = WorldFx.argb(c0, apexAlpha);

            q.vertex(x0, top, z0, WorldFx.argb(c0, op));
            q.vertex(x1, top, z1, WorldFx.argb(c1, op));
            q.vertex(cx, apexY, cz, apex);
            q.vertex(cx, apexY, cz, apex);

            double ro = r + 0.035, ri = r - 0.035;
            double ry = top + 0.002;
            int edge0 = WorldFx.argb(c0, Math.min(1.0, op + 0.25));
            int edge1 = WorldFx.argb(c1, Math.min(1.0, op + 0.25));
            q.vertex(cx + Math.cos(a0) * ro, ry, cz + Math.sin(a0) * ro, edge0);
            q.vertex(cx + Math.cos(a1) * ro, ry, cz + Math.sin(a1) * ro, edge1);
            q.vertex(cx + Math.cos(a1) * ri, ry, cz + Math.sin(a1) * ri, edge1);
            q.vertex(cx + Math.cos(a0) * ri, ry, cz + Math.sin(a0) * ri, edge0);
        }
    }
}
