package com.example.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class VisualsClient implements ClientModInitializer {
    public static final List<Module> MODULES = new ArrayList<>();
    public static Module WATERMARK, ARRAY_LIST, TARGET_HUD, TOTEM_BAR, FULLBRIGHT, HIT_PARTICLES, HAND, TITLE_SCREEN;
    public static KeyBinding OPEN_GUI;

    // simple settings
    public static float particleCount = 14f;
    public static float totemScale = 1f, totemX = 0.5f, totemY = 0.72f;

    public static LivingEntity target;
    public static long targetTime;

    private static KeyBinding bind(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.visuals." + id, InputUtil.Type.KEYSYM, key, "category.visuals"));
    }

    @Override
    public void onInitializeClient() {
        OPEN_GUI = bind("gui", GLFW.GLFW_KEY_RIGHT_SHIFT);

        WATERMARK = add(new Module("Watermark", null, true)
                .desc("Name, FPS and accent line in the corner."));
        ARRAY_LIST = add(new Module("ArrayList", bind("arraylist", GLFW.GLFW_KEY_H), true)
                .desc("List of enabled modules on the right."));
        TARGET_HUD = add(new Module("TargetHUD", bind("targethud", GLFW.GLFW_KEY_K), true)
                .desc("Name and health of the entity you hit."));
        TOTEM_BAR = add(new Module("TotemBar", bind("totembar", GLFW.GLFW_KEY_B), true)
                .desc("Totem of Undying count in your inventory.")
                .slider("Scale", 0.5f, 2f, () -> totemScale, v -> totemScale = v)
                .slider("Position X", 0f, 1f, () -> totemX, v -> totemX = v)
                .slider("Position Y", 0f, 1f, () -> totemY, v -> totemY = v)
                .reset(() -> { totemScale = 1f; totemX = 0.5f; totemY = 0.72f; }));
        FULLBRIGHT = add(new Module("Fullbright", bind("fullbright", GLFW.GLFW_KEY_J), false)
                .desc("Constant night vision, visible only to you."));
        HIT_PARTICLES = add(new Module("HitParticles", bind("hitparticles", GLFW.GLFW_KEY_L), true)
                .desc("Particles when you hit an entity.")
                .slider("Amount", 4f, 40f, () -> particleCount, v -> particleCount = v)
                .reset(() -> particleCount = 14f));
        HAND = add(new Module("HandCustomizer", bind("hand", GLFW.GLFW_KEY_G), true)
                .desc("Position, rotation, size and swing speed of your hand.")
                .slider("Pos X", -1f, 1f, () -> HandConfig.x, v -> HandConfig.x = v)
                .slider("Pos Y", -1f, 1f, () -> HandConfig.y, v -> HandConfig.y = v)
                .slider("Pos Z", -1f, 1f, () -> HandConfig.z, v -> HandConfig.z = v)
                .slider("Rot X", -180f, 180f, () -> HandConfig.rotX, v -> HandConfig.rotX = v)
                .slider("Rot Y", -180f, 180f, () -> HandConfig.rotY, v -> HandConfig.rotY = v)
                .slider("Rot Z", -180f, 180f, () -> HandConfig.rotZ, v -> HandConfig.rotZ = v)
                .slider("Scale", 0.3f, 1.8f, () -> HandConfig.scale, v -> HandConfig.scale = v)
                .slider("Swing time", 0.5f, 3f, () -> HandConfig.swing, v -> HandConfig.swing = v)
                .reset(HandConfig::reset));

        TITLE_SCREEN = add(new Module("TitleScreen", null, true)
                .desc("Custom Spike main menu (Singleplayer / Multiplayer)."));

        HandConfig.load();
        ModConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Module m : MODULES) {
                if (m.key != null) while (m.key.wasPressed()) m.toggle();
            }
            while (OPEN_GUI.wasPressed()) client.setScreen(new ClickGuiScreen());
            if (TITLE_SCREEN.enabled && client.currentScreen != null
                    && client.currentScreen.getClass() == TitleScreen.class) {
                client.setScreen(new SpikeTitleScreen());
            }
            HandConfig.enabled = HAND.enabled;

            if (client.player != null) {
                if (FULLBRIGHT.enabled) {
                    client.player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
                } else {
                    var eff = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                    if (eff != null && eff.getDuration() <= 400 && !eff.shouldShowIcon()) {
                        client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                    }
                }
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity le) {
                target = le;
                targetTime = System.currentTimeMillis();
                if (HIT_PARTICLES.enabled) {
                    int n = (int) particleCount;
                    for (int i = 0; i < n; i++) {
                        double dx = (world.random.nextDouble() - 0.5) * 0.8;
                        double dy = world.random.nextDouble() * le.getHeight();
                        double dz = (world.random.nextDouble() - 0.5) * 0.8;
                        world.addParticle(i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.CRIT,
                                le.getX() + dx, le.getY() + dy, le.getZ() + dz, dx * 0.2, 0.1, dz * 0.2);
                    }
                }
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
    }

    private static Module add(Module m) {
        MODULES.add(m);
        return m;
    }
}
