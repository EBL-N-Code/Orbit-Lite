package org.skyblockagent.render;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector4f;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.mixins.accessors.DrawContextAccessor;
import org.skyblockagent.render.states.CircleShapeRenderState;
import org.skyblockagent.render.states.RectangleShapeRenderState;

/**
 * Utility class for rendering Text and 2D shapes in Minecraft's GUI.
 */
public class Renderer {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final StyleSpriteSource FONT = new StyleSpriteSource.Font(Identifier.of(Skyblockagent.NAMESPACE, "custom_font"));
    private static final Style FONT_STYLE = Style.EMPTY.withFont(FONT);
    public static final int WHITE_COLOR = 0xFFE0E0E0;

    public static void drawCircle(DrawContext context, int x, int y, int width, int height, int color, int borderColor, float borderThickness) {
        ((DrawContextAccessor) context).getState().addSimpleElement(new CircleShapeRenderState(
                context, x, y, width, height, color,
                borderColor, (float) ((borderThickness * 1.5) / Math.min(width, height))
        ));
    }

    public static void drawCircle(DrawContext context, int x, int y, int width, int height, int color) {
        drawCircle(context, x, y, width, height, color, 0, 0f);
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, Vector4f roundness, int color, int borderColor, float borderThickness) {
        ((DrawContextAccessor) context).getState().addSimpleElement(new RectangleShapeRenderState(
                context, x, y, width, height,
                roundness, color, borderColor, borderThickness
        ));
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, float roundness, int color, int borderColor, float borderThickness) {
        drawRect(context, x, y, width, height, new Vector4f(roundness), color, borderColor, borderThickness);
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, Vector4f roundness, int color) {
        drawRect(context, x, y, width, height, roundness, color, 0, 0f);
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, float roundness, int color) {
        drawRect(context, x, y, width, height, new Vector4f(roundness), color, 0, 0f);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, Vector4f roundness, float thickness, int color) {
        drawRect(context, x, y, width, height, roundness, 0, color, thickness);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, float roundness, float thickness, int color) {
        drawRect(context, x, y, width, height, new Vector4f(roundness), 0, color, thickness);
    }

    public static void drawText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        context.drawText(TEXT_RENDERER, Text.literal(text).setStyle(FONT_STYLE), x, y, color, shadow);
    }

    public static void drawText(DrawContext context, MutableText text, int x, int y) {
        context.drawText(TEXT_RENDERER, text.setStyle(text.getStyle().withFont(FONT)), x, y, WHITE_COLOR, false);
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
        drawText(context, text, centerX - TEXT_RENDERER.getWidth(text) / 2, y, WHITE_COLOR, false);
    }
}