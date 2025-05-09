package org.skyblockagent.utils.render;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2f;
import org.skyblockagent.mixins.accessors.DrawContextAccessor;

/**
 * Utility class for rendering 2D shapes in Minecraft's GUI, including ellipses and rounded rectangles.
 */
public class RenderUtils {

    /**
     * Draws an ellipse.
     * Supports an outline with specified thickness and color.
     *
     * @param context         The {@link DrawContext} for rendering.
     * @param x               The x-coordinate of the top-left corner in screen coordinates.
     * @param y               The y-coordinate of the top-left corner in screen coordinates.
     * @param width           The width of the ellipse (including outline).
     * @param height          The height of the ellipse (including outline).
     * @param color           The ARGB color for the ellipse's fill.
     * @param outlineThickness The thickness of the outline (in pixels).
     * @param outlineColor    The ARGB color for the outline.
     */
    public static void drawEllipse(DrawContext context, float x, float y, float width, float height, int color, float outlineThickness, int outlineColor) {
        VertexConsumerProvider.Immediate consumers = ((DrawContextAccessor) context).getVertexConsumers();
        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayers.ELLIPSE_QUAD_LAYER);
        MatrixStack.Entry transform = context.getMatrices().peek();

        if (outlineThickness > 0) {
            drawTexturedQuad(vertexConsumer, transform, x, y, width, height, outlineColor);

            width -= 2 * outlineThickness;
            height -= 2 * outlineThickness;
            x += outlineThickness;
            y += outlineThickness;
        }

        drawTexturedQuad(vertexConsumer, transform, x, y, width, height, color);
    }

    /**
     * Draws an ellipse without an outline.
     *
     * @param context The {@link DrawContext} for rendering.
     * @param x       The x-coordinate of the top-left corner in screen coordinates.
     * @param y       The y-coordinate of the top-left corner in screen coordinates.
     * @param width   The width of the ellipse.
     * @param height  The height of the ellipse.
     * @param color   The ARGB color for the ellipse's fill.
     */
    public static void drawEllipse(DrawContext context, float x, float y, float width, float height, int color) {
        drawEllipse(context, x, y, width, height, color, 0f, 0);
    }

    /**
     * Draws a rounded rectangle.
     * Supports an outline with specified thickness and color.
     * All corners have the same roundness radius.
     *
     * @param context         The {@link DrawContext} for rendering.
     * @param x               The x-coordinate of the top-left corner in screen coordinates.
     * @param y               The y-coordinate of the top-left corner in screen coordinates.
     * @param width           The width of the rectangle (including outline).
     * @param height          The height of the rectangle (including outline).
     * @param roundness       The corner radius for all corners (in pixels).
     * @param color           The ARGB color for the rectangle's fill.
     * @param outlineThickness The thickness of the outline (in pixels).
     * @param outlineColor    The ARGB color for the outline.
     */
    public static void drawRoundedRect(DrawContext context, float x, float y, float width, float height, float roundness, int color, float outlineThickness, int outlineColor) {
        VertexConsumerProvider.Immediate consumers = ((DrawContextAccessor) context).getVertexConsumers();
        UniformRenderLayer renderLayer = (UniformRenderLayer) RenderLayers.ROUND_RECT_LAYERS.apply(roundness, new Vector2f(width, height));
        VertexConsumer vertexConsumer = consumers.getBuffer((RenderLayer) renderLayer);
        MatrixStack.Entry transform = context.getMatrices().peek();

        if (outlineThickness > 0) {
            drawTexturedQuad(vertexConsumer, transform, x, y, width, height, outlineColor);

            width -= 2 * outlineThickness;
            height -= 2 * outlineThickness;
            x += outlineThickness;
            y += outlineThickness;
        }

        drawTexturedQuad(vertexConsumer, transform, x, y, width, height, color);
    }

    /**
     * Draws a rounded rectangle without an outline.
     *
     * @param context   The {@link DrawContext} for rendering.
     * @param x         The x-coordinate of the top-left corner in screen coordinates.
     * @param y         The y-coordinate of the top-left corner in screen coordinates.
     * @param width     The width of the rectangle.
     * @param height    The height of the rectangle.
     * @param roundness The corner radius for all corners (in pixels).
     * @param color     The ARGB color for the rectangle's fill.
     */
    public static void drawRoundedRect(DrawContext context, float x, float y, float width, float height, float roundness, int color) {
        drawRoundedRect(context, x, y, width, height, roundness, color, 0f, 0);
    }

    /**
     * Draws a quad with the specified vertices, texture coordinates, and color.
     *
     * @param vertexConsumer The {@link VertexConsumer} to write vertices to.
     * @param transform      The {@link MatrixStack.Entry} for the current transformation matrix.
     * @param x              The x-coordinate of the top-left corner.
     * @param y              The y-coordinate of the top-left corner.
     * @param width          The width of the quad.
     * @param height         The height of the quad.
     * @param color          The ARGB color for the quad.
     */
    private static void drawTexturedQuad(VertexConsumer vertexConsumer, MatrixStack.Entry transform, float x, float y, float width, float height, int color) {
        vertexConsumer.vertex(transform, x, y + height, 0).texture(0, 0).color(color);
        vertexConsumer.vertex(transform, x + width, y + height, 0).texture(1, 0).color(color);
        vertexConsumer.vertex(transform, x + width, y, 0).texture(1, 1).color(color);
        vertexConsumer.vertex(transform, x, y, 0).texture(0, 1).color(color);
    }
}