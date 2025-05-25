package org.skyblockagent.utils.render;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.skyblockagent.mixins.accessors.DrawContextAccessor;

/**
 * Utility class for rendering 2D shapes in Minecraft's GUI, including ellipses and rectangles.
 */
public class RenderUtils {
    public static void drawEllipse(DrawContext context, float x, float y, float width, float height, int fillColor, float outlineThickness, int outlineColor) {
        VertexConsumerProvider.Immediate consumers = ((DrawContextAccessor) context).getVertexConsumers();
        RenderLayer renderLayer = RenderLayers.getEllipseLayer(fillColor, outlineColor, (float) ((outlineThickness * 1.5) / Math.min(width, height)));
        VertexConsumer vertexConsumer = consumers.getBuffer(renderLayer);
        MatrixStack.Entry transform = context.getMatrices().peek();

        drawTexturedQuad(vertexConsumer, transform, x, y, width, height);
    }

    public static void drawEllipse(DrawContext context, float x, float y, float width, float height, int color) {
        drawEllipse(context, x, y, width, height, color, 0f, 0);
    }

    public static void drawEllipseBorder(DrawContext context, float x, float y, float width, float height, float thickness, int color) {
        drawEllipse(context, x, y, width, height, 0x00000000, thickness, color);
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Vector4f roundness, int fillColor, float outlineThickness, int outlineColor) {
        VertexConsumerProvider.Immediate consumers = ((DrawContextAccessor) context).getVertexConsumers();
        RenderLayer renderLayer = RenderLayers.getRectLayer(fillColor, outlineColor, outlineThickness, roundness, new Vector2f(width, height));
        VertexConsumer vertexConsumer = consumers.getBuffer(renderLayer);
        MatrixStack.Entry transform = context.getMatrices().peek();

        drawTexturedQuad(vertexConsumer, transform, x, y, width, height);
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float roundness, int fillColor, float outlineThickness, int outlineColor) {
        drawRect(context, x, y, width, height, new Vector4f(roundness), fillColor, outlineThickness, outlineColor);
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, Vector4f roundness, int color) {
        drawRect(context, x, y, width, height, roundness, color, 0f, 0);
    }

    public static void drawRect(DrawContext context, float x, float y, float width, float height, float roundness, int color) {
        drawRect(context, x, y, width, height, roundness, color, 0f, 0);
    }

    public static void drawBorder(DrawContext context, float x, float y, float width, float height, Vector4f roundness, float thickness, int color) {
        drawRect(context, x, y, width, height, roundness, 0x00000000, thickness, color);
    }

    public static void drawBorder(DrawContext context, float x, float y, float width, float height, float roundness, float thickness, int color) {
        drawRect(context, x, y, width, height, roundness, 0x00000000, thickness, color);
    }

    private static void drawTexturedQuad(VertexConsumer vertexConsumer, MatrixStack.Entry transform, float x, float y, float width, float height) {
        vertexConsumer.vertex(transform, x, y + height, 0).texture(0, 0);
        vertexConsumer.vertex(transform, x + width, y + height, 0).texture(1, 0);
        vertexConsumer.vertex(transform, x + width, y, 0).texture(1, 1);
        vertexConsumer.vertex(transform, x, y, 0).texture(0, 1);
    }
}