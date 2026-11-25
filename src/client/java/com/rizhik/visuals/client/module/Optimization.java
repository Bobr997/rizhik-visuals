package com.rizhik.visuals.client.module;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Optimization extends Module {
    private static final Path BACKUP =
            FabricLoader.getInstance().getConfigDir().resolve("rizhikvisuals_optimization_backup.properties");

    private final Setting<Double> renderDist = add(new Setting<Double>("Дальность", 8.0, 4, 16));
    private final Setting<Double> simDist = add(new Setting<Double>("Симуляция", 6.0, 5, 12));
    private final Setting<Double> entityDist = add(new Setting<Double>("Сущности", 0.6, 0.5, 1.5));
    private final Setting<Boolean> noClouds = add(new Setting<Boolean>("Без облаков", true));
    private final Setting<Boolean> minParticles = add(new Setting<Boolean>("Мин. частицы", true));
    private final Setting<Boolean> noShadows = add(new Setting<Boolean>("Без теней", true));
    private final Setting<Boolean> noBlend = add(new Setting<Boolean>("Без биом-блюра", true));

    private Properties orig;
    private int tick;

    public Optimization() {
        super("Optimization", Category.MISC,
                "Поднимает FPS: ограничивает дальность прорисовки и симуляции, убирает облака, тени и лишние частицы. При выключении все настройки возвращаются как были.");
    }

    private static String particleName(Options o) {
        return ((Enum<?>) o.particles().get()).name();
    }

    private static Object particleConst(Options o, String name) {
        Enum<?> cur = (Enum<?>) o.particles().get();
        for (Object c : cur.getDeclaringClass().getEnumConstants()) {
            if (((Enum<?>) c).name().equals(name)) return c;
        }
        return cur;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setParticles(Options o, Object value) {
        ((OptionInstance) o.particles()).set(value);
    }

    private static Properties readBackup() {
        if (!Files.exists(BACKUP)) return null;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(BACKUP, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return null;
        }
        return p.containsKey("rd") ? p : null;
    }

    private static void writeBackup(Properties p) {
        try (Writer w = Files.newBufferedWriter(BACKUP, StandardCharsets.UTF_8)) {
            p.store(w, "original video settings");
        } catch (IOException ignored) {
        }
    }

    private static Properties snapshot(Options o) {
        Properties p = new Properties();
        p.setProperty("rd", String.valueOf(o.renderDistance().get()));
        p.setProperty("sd", String.valueOf(o.simulationDistance().get()));
        p.setProperty("ed", String.valueOf(o.entityDistanceScaling().get()));
        p.setProperty("cloud", o.cloudStatus().get().name());
        p.setProperty("part", particleName(o));
        p.setProperty("shadows", String.valueOf(o.entityShadows().get()));
        p.setProperty("blend", String.valueOf(o.biomeBlendRadius().get()));
        return p;
    }

    private static void restore(Options o, Properties p) {
        try {
            o.renderDistance().set(Integer.parseInt(p.getProperty("rd")));
            o.simulationDistance().set(Integer.parseInt(p.getProperty("sd")));
            o.entityDistanceScaling().set(Double.parseDouble(p.getProperty("ed")));
            o.cloudStatus().set(CloudStatus.valueOf(p.getProperty("cloud")));
            setParticles(o, particleConst(o, p.getProperty("part")));
            o.entityShadows().set(Boolean.parseBoolean(p.getProperty("shadows")));
            o.biomeBlendRadius().set(Integer.parseInt(p.getProperty("blend")));
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    protected void onEnable() {
        Options o = Minecraft.getInstance().options;
        orig = readBackup();
        if (orig == null) {
            orig = snapshot(o);
            writeBackup(orig);
        }
        apply(o);
    }

    @Override
    protected void onDisable() {
        Options o = Minecraft.getInstance().options;
        Properties b = readBackup();
        if (b != null) {
            restore(o, b);
            try {
                Files.deleteIfExists(BACKUP);
            } catch (IOException ignored) {
            }
        }
        orig = null;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (orig != null && ++tick % 20 == 0) apply(mc.options);
    }

    private void apply(Options o) {
        if (orig == null) return;
        try {
            int rd = (int) Math.min(Integer.parseInt(orig.getProperty("rd")), Math.round(renderDist.get()));
            if (o.renderDistance().get() != rd) o.renderDistance().set(rd);

            int sd = (int) Math.min(Integer.parseInt(orig.getProperty("sd")), Math.round(simDist.get()));
            if (o.simulationDistance().get() != sd) o.simulationDistance().set(sd);

            double ed = Math.min(Double.parseDouble(orig.getProperty("ed")), entityDist.get());
            if (Math.abs(o.entityDistanceScaling().get() - ed) > 0.001) o.entityDistanceScaling().set(ed);

            CloudStatus cs = noClouds.get() ? CloudStatus.OFF : CloudStatus.valueOf(orig.getProperty("cloud"));
            if (o.cloudStatus().get() != cs) o.cloudStatus().set(cs);

            String wantParticles = minParticles.get() ? "MINIMAL" : orig.getProperty("part");
            if (!particleName(o).equals(wantParticles)) setParticles(o, particleConst(o, wantParticles));

            boolean sh = noShadows.get() ? false : Boolean.parseBoolean(orig.getProperty("shadows"));
            if (o.entityShadows().get() != sh) o.entityShadows().set(sh);

            int bl = noBlend.get() ? 0 : Integer.parseInt(orig.getProperty("blend"));
            if (o.biomeBlendRadius().get() != bl) o.biomeBlendRadius().set(bl);
        } catch (RuntimeException ignored) {
        }
    }
}
