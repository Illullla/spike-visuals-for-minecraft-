package com.example.visuals.mixin;

import com.example.visuals.HandConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void visuals$swing(CallbackInfoReturnable<Integer> cir) {
        if (!HandConfig.enabled) return;
        if ((Object) this != MinecraftClient.getInstance().player) return; // only your own hand animation
        cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValue() * HandConfig.swing)));
    }
}
