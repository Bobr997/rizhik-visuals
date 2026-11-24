package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.gui.Render;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Markers extends Module {
    public record Marker(String name, double x, double y, double z) {}

    public static final List<Marker> LIST = new ArrayList<>();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("rizhikvisuals_markers.txt");

    private final Setting<Boolean> showDistance = add(new Setting<Boolean>("Показывать дистанцию", true));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.07f));

    public Markers() {
        super("Markers", Category.MARKERS,
                "Метки с названием и координатами, видны на экране с дистанцией. Редактор меток открывается клавишей Right Alt.");
        load();
    }

    public static void save() {
        List<String> out = new ArrayList<>();
        for (Marker m : LIST) {
            out.add(m.name().replace("\t", " ") + "\t" + m.x() + "\t" + m.y() + "\t" + m.z());
        }
        try {
            Files.write(FILE, out);
        } catch (IOException ignored) {
        }
    }

    private static void load() {
        LIST.clear();
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE)) {
                String[] p = line.split("\t");
                if (p.length == 4) {
                    LIST.add(new Marker(p[0], Double.parseDouble(p[1]),
                            Double.parseDouble(p[2]), Double.parseDouble(p[3])));
                }
            }
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    @Override
    public void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || LIST.isEmpty()) return;
        Font f = mc.font;
        float pt = Frame.partial;

        Vec3 eye = mc.player.getEyePosition(pt);
        double yaw = Math.toRadians(mc.player.getViewYRot(pt));
        double pitch = Math.toRadians(mc.player.getViewXRot(pt));

        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double ux = -Math.sin(yaw) * Math.sin(pitch);
        double uy = Math.cos(pitch);
        double uz = Math.cos(yaw) * Math.sin(pitch);

        int W = g.guiWidth(), H = g.guiHeight();
        double fov = mc.options.fov().get();
        double scale = (H / 2.0) / Math.tan(Math.toRadians(fov) / 2.0);

        for (Marker m : LIST) {
            double dx = m.x() - eye.x, dy = m.y() - eye.y, dz = m.z() - eye.z;
            double cx = dx * rx + dz * rz;
            double cy = dx * ux + dy * uy + dz * uz;
            double cz = dx * fx + dy * fy + dz * fz;
            if (cz < 0.05) cz = 0.05;

            int sx = (int) Math.round(W / 2.0 + cx / cz * scale);
            int sy = (int) Math.round(H / 2.0 - cy / cz * scale);
            sx = Math.max(24, Math.min(W - 24, sx));
            sy = Math.max(24, Math.min(H - 24, sy));

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            String label = showDistance.get() ? m.name() + " | " + (int) dist + " м" : m.name();
            int tw = f.width(label);

            Render.roundedRect(g, sx - tw / 2 - 4, sy - 20, tw + 8, 13, 4, 0xAA111114);
            g.drawString(f, label, sx - tw / 2, sy - 17, 0xFFFFFFFF);
            Render.roundedRect(g, sx - 3, sy - 3, 6, 6, 3, color.rgb());
        }
    }
}
