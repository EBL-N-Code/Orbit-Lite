package org.orbit.render.states;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import org.orbit.render.CustomPipelines;
import org.orbit.render.DirectVertexConsumer;

public class CircleShapeRenderState extends SimpleShapeRenderState {
    public CircleShapeRenderState(DrawContext context, int x, int y, int w, int h, int color, int borderColor, float borderThickness) {
        super(CustomPipelines.CIRCLE_PIPELINE, context, x, y, w, h, color, borderColor, borderThickness);
    }

    @Override
    public void setupVertices(VertexConsumer vertices) {
        DirectVertexConsumer dvc = new DirectVertexConsumer((BufferBuilder) vertices);
        dvc.vertex(pose, x, y + h).texture(0, 0).color(color).putColor(borderColor).putFloat(borderThickness);
        dvc.vertex(pose, x + w, y + h).texture(1, 0).color(color).putColor(borderColor).putFloat(borderThickness);
        dvc.vertex(pose, x + w, y).texture(1, 1).color(color).putColor(borderColor).putFloat(borderThickness);
        dvc.vertex(pose, x, y).texture(0, 1).color(color).putColor(borderColor).putFloat(borderThickness);
    }
}
