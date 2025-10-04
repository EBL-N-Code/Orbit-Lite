package org.skyblockagent.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import org.skyblockagent.render.UniformRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin for {@link net.minecraft.client.render.RenderLayer.MultiPhase} to implement
 * {@link UniformRenderLayer}, enabling custom uniform setting for shader programs.
 */
@Mixin(RenderLayer.MultiPhase.class)
public class RenderLayerMixin implements UniformRenderLayer {
    @Unique
    private final Map<String, Object> uniforms = new HashMap<>();

    @Override
    public void setUniform(String u, Object v) {
        if (v == null) uniforms.remove(u);
        else uniforms.put(u, v);
    }

    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(II)V"))
    void beforeDraw(BuiltBuffer buffer, CallbackInfo ci, @Local(ordinal = 0) RenderPass pass) {
        uniforms.forEach((name, value) -> {
            switch (value) {
                case float[] floats -> pass.setUniform(name, floats);
                case Float floot -> pass.setUniform(name, floot);
                default -> throw new IllegalArgumentException("Unsupported uniform type: " + value.getClass());
            }// default -> {}
        });
    }
}