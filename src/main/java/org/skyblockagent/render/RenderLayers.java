package org.skyblockagent.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Defines custom {@link net.minecraft.client.render.RenderLayer}s for rendering 2D shapes in Minecraft's GUI.
 * Configures rendering pipelines with specific vertex formats, shaders, and blending settings.
 */
public class RenderLayers {
    public static final Supplier<RenderLayer> ELLIPSE_QUAD_LAYER = () -> RenderLayer.of(
            Skyblockagent.NAMESPACE + "/quad_ellipse",
            1024,
            false,
            false,
            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
                    .withVertexShader("core/position_tex")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                    .withCull(true)
                    .withFragmentShader(Identifier.of(Skyblockagent.NAMESPACE, "ellipse_fragment"))
                    .withLocation(Identifier.of(Skyblockagent.NAMESPACE, "quad_ellipse"))
                    .withUniform("FillColor", UniformType.VEC4)
                    .withUniform("OutlineColor", UniformType.VEC4)
                    .withUniform("Thickness", UniformType.FLOAT)
                    .build()),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );
    public static final Supplier<RenderLayer> ROUND_RECT_LAYER = () -> RenderLayer.of(
            Skyblockagent.NAMESPACE + "/quad_round_rect",
            1024,
            false,
            false,
            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
                    .withVertexShader("core/position_tex")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                    .withCull(true)
                    .withFragmentShader(Identifier.of(Skyblockagent.NAMESPACE, "round_rect_fragment"))
                    .withLocation(Identifier.of(Skyblockagent.NAMESPACE, "quad_round_rect"))
                    .withUniform("FillColor", UniformType.VEC4)
                    .withUniform("OutlineColor", UniformType.VEC4)
                    .withUniform("Thickness", UniformType.FLOAT)
                    .withUniform("Roundness", UniformType.VEC4)
                    .withUniform("QuadDimensions", UniformType.VEC2)
                    .build()),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );
    private static final Map<String, RenderLayer> LAYER_CACHE = new HashMap<>();

    public static RenderLayer getEllipseLayer(int fillColor, int outlineColor, float thickness) {
        String key = "Ellipse|" + thickness + "|" + fillColor + "|" + outlineColor;
        return LAYER_CACHE.computeIfAbsent(key, _ -> {
            UniformRenderLayer renderLayer = (UniformRenderLayer) ELLIPSE_QUAD_LAYER.get();
            renderLayer.setUniform("FillColor", ColorUtils.colorToVec4(fillColor));
            renderLayer.setUniform("OutlineColor", ColorUtils.colorToVec4(outlineColor));
            renderLayer.setUniform("Thickness", thickness);
            return (RenderLayer) renderLayer;
        });
    }

    public static RenderLayer getRectLayer(int fillColor, int outlineColor, float thickness, Vector4f roundness, Vector2f quadDimensions) {
        String key = "Rect|" + thickness + "|" + fillColor + "|" + outlineColor + "|" + roundness.toString() + "|" + quadDimensions.toString();
        return LAYER_CACHE.computeIfAbsent(key, _ -> {
            UniformRenderLayer renderLayer = (UniformRenderLayer) ROUND_RECT_LAYER.get();
            renderLayer.setUniform("FillColor", ColorUtils.colorToVec4(fillColor));
            renderLayer.setUniform("OutlineColor", ColorUtils.colorToVec4(outlineColor));
            renderLayer.setUniform("Thickness", thickness);
            renderLayer.setUniform("Roundness", roundness);
            renderLayer.setUniform("QuadDimensions", quadDimensions);
            return (RenderLayer) renderLayer;
        });
    }
}
