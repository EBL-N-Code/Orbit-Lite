package org.skyblockagent.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Vector2f;
import org.skyblockagent.Skyblockagent;

import java.util.function.BiFunction;

/**
 * Defines custom {@link net.minecraft.client.render.RenderLayer}s for rendering 2D shapes in Minecraft's GUI.
 * Configures rendering pipelines with specific vertex formats, shaders, and blending settings.
 */
public class RenderLayers {

    /**
     * A {@link net.minecraft.client.render.RenderLayer} for rendering ellipses as textured quads.
     */
    public static final RenderLayer ELLIPSE_QUAD_LAYER = RenderLayer.of(
            Skyblockagent.NAMESPACE + "/quad_ellipse",
            1024,
            false,
            false,
            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
                    .withVertexShader("core/position_tex_color")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .withCull(true)
                    .withFragmentShader(Identifier.of(Skyblockagent.NAMESPACE, "ellipse_fragment"))
                    .withLocation(Identifier.of(Skyblockagent.NAMESPACE, "quad_ellipse"))
                    .build()),
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    /**
     * A function that creates {@link net.minecraft.client.render.RenderLayer}s for rounded rectangles
     * with a specific corner roundness and quad size.
     */
    public static final BiFunction<Float, Vector2f, RenderLayer> ROUND_RECT_LAYERS = Util.memoize((roundness, quadDimensions) -> {
        RenderLayer rl = RenderLayer.of(
                Skyblockagent.NAMESPACE + "/quad_round_rect",
                1024,
                false,
                false,
                RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
                        .withVertexShader("core/position_tex_color")
                        .withBlend(BlendFunction.TRANSLUCENT)
                        .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                        .withCull(true)
                        .withFragmentShader(Identifier.of(Skyblockagent.NAMESPACE, "round_rect_fragment"))
                        .withLocation(Identifier.of(Skyblockagent.NAMESPACE, "quad_round_rect"))
                        .withUniform("Roundness", UniformType.FLOAT)
                        .withUniform("QuadDimensions", UniformType.VEC2)
                        .build()),
                RenderLayer.MultiPhaseParameters.builder().build(false)
        );
        UniformRenderLayer erl = (UniformRenderLayer) rl;
        erl.setUniform("Roundness", roundness);
        erl.setUniform("QuadDimensions", quadDimensions);
        return rl;
    });
}
