package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {

    public static int accent(float offset) {
        float t = (float) ((Math.sin(System.currentTimeMillis() / 700.0 + offset * 6.0) + 1.0) / 2.0);
        int r = (int) (0x1F + (0x5C - 0x1F) * t);
        int g = (int) (0xB8 + (0xFF - 0xB8) * t);
        int b = (int) (0x5A + (0xA0 - 0x5A) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // Watermark
        if (VisualsClient.WATERMARK.enabled) {
            String text = "Spike | " + mc.player.getName().getString() + " | " + mc.getCurrentFps() + " fps";
            int w = tr.getWidth(text);
            ctx.fill(4, 4, 4 + w + 8, 18, 0x90000000);
            ctx.fill(4, 4, 4 + w + 8, 5, accent(0f));
            ctx.drawTextWithShadow(tr, "Spike", 8, 8, accent(0f));
            ctx.drawTextWithShadow(tr, text.substring(5), 8 + tr.getWidth("Spike"), 8, 0xFFFFFFFF);
        }

        // ArrayList
        if (VisualsClient.ARRAY_LIST.enabled) {
            List<Module> active = VisualsClient.MODULES.stream()
                    .filter(m -> m.enabled && m != VisualsClient.WATERMARK && m != VisualsClient.ARRAY_LIST && m != VisualsClient.TITLE_SCREEN)
                    .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed())
                    .toList();
            int y = 4;
            int i = 0;
            for (Module m : active) {
                int w = tr.getWidth(m.name);
                int x = sw - w - 8;
                int col = accent(i * 0.07f);
                ctx.fill(x - 2, y - 1, sw, y + 10, 0x80000000);
                ctx.fill(sw - 2, y - 1, sw, y + 10, col);
                ctx.drawTextWithShadow(tr, m.name, x, y + 1, col);
                y += 11;
                i++;
            }
        }


        // TotemBar
        if (VisualsClient.TOTEM_BAR.enabled) {
            int count = 0;
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack st = inv.getStack(i);
                if (st.isOf(Items.TOTEM_OF_UNDYING)) count += st.getCount();
            }
            boolean offhand = mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            float ratio = Math.min(1f, count / 6f);
            int col = count == 0 ? 0xFFFF4D4D : (Color.HSBtoRGB(0.05f + ratio * 0.28f, 0.75f, 1f) | 0xFF000000);

            int w = 74, h = 26;
            float sc = VisualsClient.totemScale;
            float px = sw * VisualsClient.totemX - w * sc / 2f;
            float py = sh * VisualsClient.totemY;
            var m = ctx.getMatrices();
            m.push();
            m.translate(px, py, 0);
            m.scale(sc, sc, 1f);
            ctx.fill(1, 0, w - 1, h, 0xB0101014);
            ctx.fill(0, 1, w, h - 1, 0xB0101014);
            ctx.fill(1, 0, w - 1, 1, accent(0.6f));
            ctx.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), 5, 3);
            ctx.drawTextWithShadow(tr, "x" + count, 27, 5, col);
            ctx.fill(27, 17, w - 8, 20, 0xFF26262E);
            ctx.fill(27, 17, 27 + (int) ((w - 35) * ratio), 20, col);
            ctx.fill(w - 7, 5, w - 3, 9, offhand ? 0xFF4DFF88 : 0xFF55555F);
            m.pop();
        }

        // TargetHUD
        LivingEntity t = VisualsClient.target;
        if (VisualsClient.TARGET_HUD.enabled && t != null) {
            boolean expired = System.currentTimeMillis() - VisualsClient.targetTime > 5000;
            if (expired || t.isRemoved() || !t.isAlive()) {
                VisualsClient.target = null;
            } else {
                int x = sw / 2 + 20;
                int y = sh / 2 + 20;
                int w = 110, h = 34;
                ctx.fill(x, y, x + w, y + h, 0x90000000);
                ctx.fill(x, y, x + w, y + 1, accent(0.3f));
                ctx.drawTextWithShadow(tr, t.getName().getString(), x + 6, y + 6, 0xFFFFFFFF);

                float hp = t.getHealth();
                float max = Math.max(1f, t.getMaxHealth());
                float ratio = Math.min(1f, hp / max);
                int barW = w - 12;
                int barCol = Color.HSBtoRGB(ratio * 0.33f, 0.8f, 1f) | 0xFF000000;
                ctx.fill(x + 6, y + 20, x + 6 + barW, y + 26, 0xFF202020);
                ctx.fill(x + 6, y + 20, x + 6 + (int) (barW * ratio), y + 26, barCol);
                String hpText = String.format("%.1f", hp);
                ctx.drawTextWithShadow(tr, hpText, x + w - 6 - tr.getWidth(hpText), y + 6, barCol);
            }
        }
    }
}
