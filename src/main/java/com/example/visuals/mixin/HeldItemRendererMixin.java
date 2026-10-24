package com.example.visuals.mixin;

import com.example.visuals.HandConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void visuals$pre(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                             float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.push();
        if (!HandConfig.enabled) return;
        float side = hand == Hand.MAIN_HAND ? 1f : -1f; // mirror for off hand
        matrices.translate(HandConfig.x * side, HandConfig.y, HandConfig.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(HandConfig.rotX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(HandConfig.rotY * side));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(HandConfig.rotZ * side));
        matrices.scale(HandConfig.scale, HandConfig.scale, HandConfig.scale);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void visuals$post(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                              float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
