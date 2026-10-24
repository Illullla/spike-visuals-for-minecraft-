package com.example.visuals;

import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Module {
    public record Setting(String name, float min, float max, Supplier<Float> get, Consumer<Float> set) {}

    public final String name;
    public final KeyBinding key; // null = no hotkey
    public boolean enabled;
    public String desc = "";
    public float anim; // 0..1 for the GUI switch animation
    public Runnable reset;
    public final List<Setting> settings = new ArrayList<>();

    public Module(String name, KeyBinding key, boolean enabled) {
        this.name = name;
        this.key = key;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }

    public Module desc(String d) {
        this.desc = d;
        return this;
    }

    public Module slider(String n, float min, float max, Supplier<Float> g, Consumer<Float> s) {
        settings.add(new Setting(n, min, max, g, s));
        return this;
    }

    public Module reset(Runnable r) {
        this.reset = r;
        return this;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
