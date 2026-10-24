package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SpikeTitleScreen extends Screen {
    private static final int GREEN = 0xFF2BE07A;

    private record Btn(String label, int x, int y, int w, int h, Runnable action) {
        boolean in(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private final List<Btn> btns = new ArrayList<>();
    private final float[] hover = new float[8];

    public SpikeTitleScreen() {
        super(Text.literal("Spike"));
    }

    @Override
    protected void init() {
        btns.clear();
        int bw = 230, bh = 30, x = (width - bw) / 2, y = height / 2 - 6;
        btns.add(new Btn("Singleplayer", x, y, bw, bh, () -> client.setScreen(new SelectWorldScreen(this))));
        btns.add(new Btn("Multiplayer", x, y + 36, bw, bh, () -> client.setScreen(new MultiplayerScreen(this))));
        btns.add(new Btn("Options", x, y + 72, bw / 2 - 3, bh, () -> client.setScreen(new OptionsScreen(this, client.options))));
        btns.add(new Btn("Quit", x + bw / 2 + 3, y + 72, bw / 2 - 3, bh, () -> client.scheduleStop()));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        // we draw our own background in render()
    }

    private static int lerpArgb(int a, int b, float t) {
        int out = 0;
        for (int shift = 24; shift >= 0; shift -= 8) {
            int ca = (a >> shift) & 255, cb = (b >> shift) & 255;
            out |= ((int) (ca + (cb - ca) * t) & 255) << shift;
        }
        return out;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        long t = System.currentTimeMillis();

        // background
        c.fillGradient(0, 0, width, height, 0xFF040806, 0xFF0A1C11);

        // animated spikes at the bottom
        int sw = 34;
        for (int i = 0; i * sw < width + sw; i++) {
            int cx = i * sw + sw / 2;
            float hgt = 34 + 24 * (float) Math.sin(t / 900.0 + i * 0.7) + 14 * (float) Math.sin(t / 1700.0 + i * 1.9);
            for (int j = 0; j < (int) hgt; j += 2) {
                int half = (int) ((sw / 2f) * (1f - j / hgt));
                if (half <= 0) break;
                c.fill(cx - half, height - j - 2, cx + half, height - j, 0x262BE07A);
            }
        }

        // title
        var m = c.getMatrices();
        String name = "SPIKE";
        float s = 6f;
        float tw = textRenderer.getWidth(name) * s;
        int ty = Math.max(14, height / 2 - 118);
        m.push();
        m.translate(width / 2f - tw / 2f + 2, ty + 2, 0);
        m.scale(s, s, 1f);
        c.drawText(textRenderer, name, 0, 0, 0x552BE07A, false);
        m.pop();
        m.push();
        m.translate(width / 2f - tw / 2f, ty, 0);
        m.scale(s, s, 1f);
        c.drawTextWithShadow(textRenderer, name, 0, 0, HudRenderer.accent(0f));
        m.pop();

        String sub = "V I S U A L S";
        float ss = 1.6f;
        float subw = textRenderer.getWidth(sub) * ss;
        m.push();
        m.translate(width / 2f - subw / 2f, ty + 56, 0);
        m.scale(ss, ss, 1f);
        c.drawTextWithShadow(textRenderer, sub, 0, 0, 0xFFB5F5CF);
        m.pop();
        int lw = 120;
        for (int i = 0; i < lw; i++) {
            c.fill(width / 2 - lw / 2 + i, ty + 74, width / 2 - lw / 2 + i + 1, ty + 75, HudRenderer.accent(i * 0.01f));
        }

        // buttons
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            boolean hv = b.in(mx, my);
            hover[i] += ((hv ? 1f : 0f) - hover[i]) * 0.25f;
            float h = hover[i];
            int bg = lerpArgb(0xE0101812, 0xE01B3A27, h);
            c.fill(b.x + 1, b.y, b.x + b.w - 1, b.y + b.h, bg);
            c.fill(b.x, b.y + 1, b.x + b.w, b.y + b.h - 1, bg);
            // left bar grows on hover, bottom line fades in
            c.fill(b.x, b.y + 3, b.x + 3 + (int) (h * 5), b.y + b.h - 3, GREEN);
            c.fill(b.x + 1, b.y + b.h - 1, b.x + b.w - 1, b.y + b.h, lerpArgb(0x002BE07A, GREEN, h));
            int col = lerpArgb(0xFFC8D4CC, 0xFFFFFFFF, h);
            c.drawTextWithShadow(textRenderer, b.label, b.x + (b.w - textRenderer.getWidth(b.label)) / 2, b.y + (b.h - 8) / 2, col);
        }

        // footer
        c.drawTextWithShadow(textRenderer, "Spike Visuals", 8, height - 14, 0xFF6F8577);
        String ver = "Minecraft 1.21.4";
        c.drawTextWithShadow(textRenderer, ver, width - textRenderer.getWidth(ver) - 8, height - 14, 0xFF6F8577);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (Btn b : btns) {
                if (b.in(mx, my)) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    b.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
