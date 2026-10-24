package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class HandConfig {
    public static boolean enabled = true;
    public static float x = 0f, y = 0f, z = 0f;
    public static float rotX = 0f, rotY = 0f, rotZ = 0f;
    public static float scale = 1f;
    public static float swing = 1f; // swing duration multiplier (>1 = slower)

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("visuals-hand.properties");
    }

    public static void reset() {
        x = y = z = 0f;
        rotX = rotY = rotZ = 0f;
        scale = 1f;
        swing = 1f;
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            Properties p = new Properties();
            p.load(r);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            x = get(p, "x", 0f); y = get(p, "y", 0f); z = get(p, "z", 0f);
            rotX = get(p, "rotX", 0f); rotY = get(p, "rotY", 0f); rotZ = get(p, "rotZ", 0f);
            scale = get(p, "scale", 1f);
            swing = get(p, "swing", 1f);
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", String.valueOf(enabled));
        p.setProperty("x", String.valueOf(x)); p.setProperty("y", String.valueOf(y)); p.setProperty("z", String.valueOf(z));
        p.setProperty("rotX", String.valueOf(rotX)); p.setProperty("rotY", String.valueOf(rotY)); p.setProperty("rotZ", String.valueOf(rotZ));
        p.setProperty("scale", String.valueOf(scale));
        p.setProperty("swing", String.valueOf(swing));
        try (Writer w = Files.newBufferedWriter(file())) {
            p.store(w, "Visuals hand customizer");
        } catch (IOException ignored) {
        }
    }

    private static float get(Properties p, String k, float d) {
        try {
            return Float.parseFloat(p.getProperty(k, String.valueOf(d)));
        } catch (NumberFormatException e) {
            return d;
        }
    }
}
