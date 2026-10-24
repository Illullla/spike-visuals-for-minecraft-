package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ModConfig {
    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("visuals.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            Properties p = new Properties();
            p.load(r);
            for (Module m : VisualsClient.MODULES) {
                m.enabled = Boolean.parseBoolean(p.getProperty("m." + m.name, String.valueOf(m.enabled)));
                m.anim = m.enabled ? 1f : 0f;
            }
            VisualsClient.particleCount = f(p, "particles", VisualsClient.particleCount);
            VisualsClient.totemScale = f(p, "totemScale", VisualsClient.totemScale);
            VisualsClient.totemX = f(p, "totemX", VisualsClient.totemX);
            VisualsClient.totemY = f(p, "totemY", VisualsClient.totemY);
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : VisualsClient.MODULES) p.setProperty("m." + m.name, String.valueOf(m.enabled));
        p.setProperty("particles", String.valueOf(VisualsClient.particleCount));
        p.setProperty("totemScale", String.valueOf(VisualsClient.totemScale));
        p.setProperty("totemX", String.valueOf(VisualsClient.totemX));
        p.setProperty("totemY", String.valueOf(VisualsClient.totemY));
        try (Writer w = Files.newBufferedWriter(file())) {
            p.store(w, "Visuals config");
        } catch (IOException ignored) {
        }
        HandConfig.save();
    }

    private static float f(Properties p, String k, float d) {
        try {
            return Float.parseFloat(p.getProperty(k, String.valueOf(d)));
        } catch (NumberFormatException e) {
            return d;
        }
    }
}
