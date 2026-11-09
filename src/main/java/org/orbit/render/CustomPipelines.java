package org.apex.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import org.apex.Apex;

public class CustomPipelines {
    public static final RenderPipeline RECT_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormat.builder()
                    .add("Position", VertexFormatElement.POSITION)
                    .add("UV0", VertexFormatElement.UV0)
                    .add("UV1", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2))
                    .add("Roundness", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .add("Color", VertexFormatElement.COLOR)
                    .add("BorderColor", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4))
                    .add("BorderThickness", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1))
                    .build(), VertexFormat.DrawMode.QUADS)
            .withCull(true)
            .withFragmentShader(Identifier.of(Apex.NAMESPACE, "rect_fragment"))
            .withVertexShader(Identifier.of(Apex.NAMESPACE, "rect_vertex"))
            .withLocation(Identifier.of(Apex.NAMESPACE, "rect"))
            .build());

    public static final RenderPipeline CIRCLE_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormat.builder()
                    .add("Position", VertexFormatElement.POSITION)
                    .add("UV0", VertexFormatElement.UV0)
                    .add("Color", VertexFormatElement.COLOR)
                    .add("BorderColor", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4))
                    .add("BorderThickness", VertexFormatElement.register(getNextVFId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1))
                    .build(), VertexFormat.DrawMode.QUADS)
            .withCull(true)
            .withFragmentShader(Identifier.of(Apex.NAMESPACE, "circle_fragment"))
            .withVertexShader(Identifier.of(Apex.NAMESPACE, "circle_vertex"))
            .withLocation(Identifier.of(Apex.NAMESPACE, "circle"))
            .build());

    private static int getNextVFId() {
        for (int i = 0; i < VertexFormatElement.MAX_COUNT; i++) {
            if (VertexFormatElement.byId(i) == null) return i;
        }
        throw new IllegalStateException("No more free VertexFormatElement slots");
    }
}
