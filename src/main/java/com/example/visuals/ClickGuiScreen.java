package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int W = 450, H = 270, SIDE = 134;
    private static final int BG = 0xF00B120E, SIDEBG = 0xFF101812, ROW_SEL = 0xFF1B2A20, ROW_HOVER = 0xFF152019;
    private static final int ACCENT = 0xFF2BE07A, OFF = 0xFF2C3A31, GRAY = 0xFF8FA396, WHITE = 0xFFFFFFFF;

    private record Hit(int x, int y, int w, int h, int kind, Object ref) {
        boolean in(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static final int SELECT = 0, TOGGLE = 1, SLIDER = 2, RESET = 3;

    private final List<Hit> hits = new ArrayList<>();
    private Module selected = VisualsClient.MODULES.get(0);
    private Hit dragging;

    public ClickGuiScreen() {
        super(Text.literal("Spike Visuals"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static void round(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 1, y, x + w - 1, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        hits.clear();
        var tr = textRenderer;
        int x0 = (width - W) / 2, y0 = (height - H) / 2;

        // window
        round(c, x0, y0, W, H, BG);
        for (int i = 0; i < W - 2; i += 3) c.fill(x0 + 1 + i, y0, x0 + 1 + Math.min(i + 3, W - 2), y0 + 2, HudRenderer.accent(i * 0.003f));
        var mt = c.getMatrices();
        mt.push();
        mt.translate(x0 + 12, y0 + 8, 0);
        mt.scale(1.5f, 1.5f, 1f);
        c.drawTextWithShadow(tr, "Spike", 0, 0, ACCENT);
        c.drawTextWithShadow(tr, "Visuals", tr.getWidth("Spike "), 0, WHITE);
        mt.pop();
        c.drawTextWithShadow(tr, "1.21.4", x0 + 12 + (int) (tr.getWidth("Spike Visuals") * 1.5f) + 6, y0 + 12, GRAY);

        // sidebar
        c.fill(x0 + 8, y0 + 28, x0 + SIDE, y0 + H - 8, SIDEBG);
        int ry = y0 + 34;
        for (Module m : VisualsClient.MODULES) {
            int rx = x0 + 12, rw = SIDE - 12;
            boolean hover = mx >= rx && mx < rx + rw && my >= ry && my < ry + 22;
            int bg = m == selected ? ROW_SEL : (hover ? ROW_HOVER : SIDEBG);
            round(c, rx, ry, rw - 4, 22, bg);
            if (m == selected) c.fill(rx, ry + 4, rx + 2, ry + 18, ACCENT);
            c.drawTextWithShadow(tr, m.name, rx + 8, ry + 7, m.enabled ? WHITE : GRAY);

            // animated switch
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.25f;
            int sx = rx + rw - 4 - 28, sy = ry + 5;
            round(c, sx, sy, 24, 12, lerp(OFF, ACCENT, m.anim));
            int kx = sx + 1 + Math.round(m.anim * 12);
            round(c, kx, sy + 1, 10, 10, WHITE);

            hits.add(new Hit(rx, ry, rw - 4, 22, SELECT, m));
            hits.add(new Hit(sx - 2, sy - 2, 28, 16, TOGGLE, m));
            ry += 25;
        }

        // settings panel
        int px = x0 + SIDE + 14, pw = W - SIDE - 24;
        c.drawTextWithShadow(tr, selected.name, px, y0 + 36, WHITE);
        c.drawTextWithShadow(tr, selected.desc, px, y0 + 50, GRAY);
        c.fill(px, y0 + 63, px + pw, y0 + 64, 0xFF1E2B22);

        if (selected.key != null) {
            String k = "Key: " + selected.key.getBoundKeyLocalizedText().getString();
            c.drawTextWithShadow(tr, k, px + pw - tr.getWidth(k), y0 + 36, GRAY);
        }

        if (selected.settings.isEmpty()) {
            c.drawTextWithShadow(tr, "No settings for this module.", px, y0 + 76, GRAY);
        }

        int sy = y0 + 72;
        for (Module.Setting s : selected.settings) {
            float v = s.get().get();
            float ratio = (v - s.min()) / (s.max() - s.min());
            ratio = Math.max(0f, Math.min(1f, ratio));
            String val = String.format("%.2f", v);
            c.drawTextWithShadow(tr, s.name(), px, sy, WHITE);
            c.drawTextWithShadow(tr, val, px + pw - tr.getWidth(val), sy, GRAY);
            int ty = sy + 13;
            round(c, px, ty, pw, 4, 0xFF1E2B22);
            int fw = Math.max(2, (int) (pw * ratio));
            round(c, px, ty, fw, 4, ACCENT);
            round(c, px + fw - 3, ty - 2, 6, 8, WHITE);
            hits.add(new Hit(px, ty - 5, pw, 14, SLIDER, s));
            sy += 26;
        }

        if (selected.reset != null) {
            int bw = 60, bh = 18, bx = px + pw - bw, by = y0 + H - 8 - bh - 4;
            boolean hv = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            round(c, bx, by, bw, bh, hv ? ROW_SEL : SIDEBG);
            c.drawTextWithShadow(tr, "Reset", bx + (bw - tr.getWidth("Reset")) / 2, by + 5, WHITE);
            hits.add(new Hit(bx, by, bw, bh, RESET, selected));
        }

        c.drawTextWithShadow(tr, "Right Shift - close", x0 + 12, y0 + H - 6 - 8 + 8 - 8, 0xFF55555F);
    }

    private void applySlider(Hit h, double mx) {
        Module.Setting s = (Module.Setting) h.ref();
        float r = (float) Math.max(0, Math.min(1, (mx - h.x()) / h.w()));
        s.set().accept(s.min() + (s.max() - s.min()) * r);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.in(mx, my)) continue;
            switch (h.kind()) {
                case SELECT -> selected = (Module) h.ref();
                case TOGGLE -> ((Module) h.ref()).toggle();
                case RESET -> ((Module) h.ref()).reset.run();
                case SLIDER -> {
                    dragging = h;
                    applySlider(h, mx);
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            applySlider(dragging, mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        ModConfig.save();
    }
}
