package org.skyblockagent.utils.render;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.mixins.accessors.DrawContextAccessor;

/**
 * Utility class for rendering Text and 2D shapes in Minecraft's GUI.
 */
public class RenderUtils {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final Identifier FONT = Identifier.of(Skyblockagent.NAMESPACE, "custom_font");
    private static final Style FONT_STYLE = Style.EMPTY.withFont(FONT);
    public static final int WHITE_COLOR = 0xFFE0E0E0;

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

    public static void drawText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        context.drawText(TEXT_RENDERER, Text.literal(text).setStyle(FONT_STYLE), x, y, color, shadow);
    }

    public static void drawText(DrawContext context, MutableText text, int x, int y, int color, boolean shadow) {
        context.drawText(TEXT_RENDERER, text.setStyle(FONT_STYLE), x, y, color, shadow);
    }

    public static void drawText(DrawContext context, String text, int x, int y, int color) {
        drawText(context, text, x, y, color, false);
    }

    public static void drawText(DrawContext context, String text, int x, int y, boolean shadow) {
        drawText(context, text, x, y, WHITE_COLOR, shadow);
    }

    public static void drawText(DrawContext context, String text, int x, int y) {
        drawText(context, text, x, y, WHITE_COLOR, false);
    }

    public static void drawCenteredText(DrawContext context, String text, int centerX, int y) {
        // Test
        drawText(context, text, centerX - TEXT_RENDERER.getWidth(text) / 2, y, WHITE_COLOR, false);
    }
}