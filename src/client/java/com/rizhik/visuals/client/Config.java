package com.rizhik.visuals.client;

import com.rizhik.visuals.client.module.HudModule;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import com.rizhik.visuals.client.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("rizhikvisuals.properties");

    private static List<Setting<?>> allSettings(Module m) {
        List<Setting<?>> all = new ArrayList<>(m.settings);
        all.addAll(m.hidden);
        return all;
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ModuleManager.all()) {
            p.setProperty(m.name + ".enabled", String.valueOf(m.isEnabled()));
            if (m instanceof HudModule h) {
                p.setProperty(m.name + ".x", String.valueOf(h.x));
                p.setProperty(m.name + ".y", String.valueOf(h.y));
                p.setProperty(m.name + ".scale", String.valueOf(h.scale.get()));
            }
            for (Setting<?> s : allSettings(m)) {
                p.setProperty(m.name + ".s." + s.name, String.valueOf(s.get()));
            }
        }
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            p.store(w, "Rizhik Visuals");
        } catch (IOException ignored) {
        }
    }

    private static int num(String raw, int fallback) {
        try {
            return raw == null ? fallback : Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return;
        }

        for (Module m : ModuleManager.all()) {
            String en = p.getProperty(m.name + ".enabled");
            if (en != null) m.setEnabled(Boolean.parseBoolean(en));

            if (m instanceof HudModule h) {
                h.x = num(p.getProperty(m.name + ".x"), h.x);
                h.y = num(p.getProperty(m.name + ".y"), h.y);
                try {
                    String sc = p.getProperty(m.name + ".scale");
                    if (sc != null) h.scale.set(Double.parseDouble(sc));
                } catch (NumberFormatException ignored) {
                }
            }

            for (Setting<?> s : allSettings(m)) {
                String raw = p.getProperty(m.name + ".s." + s.name);
                if (raw == null) continue;
                try {
                    Object v = s.get();
                    if (v instanceof Boolean) ((Setting<Boolean>) s).set(Boolean.parseBoolean(raw));
                    else if (v instanceof Double) ((Setting<Double>) s).set(Double.parseDouble(raw));
                    else if (v instanceof Float) ((Setting<Float>) s).set(Float.parseFloat(raw));
                    else if (v instanceof Integer) ((Setting<Integer>) s).set(Integer.parseInt(raw));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
