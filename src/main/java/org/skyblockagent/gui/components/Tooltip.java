package org.skyblockagent.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.render.Renderer;

import java.util.LinkedHashMap;
import java.util.Map;

public class Tooltip {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private final LinkedHashMap<String, String> lines;
    private final int width, height;

    public Tooltip(LinkedHashMap<String, String> lines) {
        this.lines = lines;
        this.height = 10 * lines.size() + 4;

        int width = 0;
        for (Map.Entry<String, String> entry : lines.entrySet()) {
            String text = entry.getKey() + entry.getValue();
            width = Math.max(width, TEXT_RENDERER.getWidth(text) + 5 + (3 * 2));
        }
        this.width = width;
    }

    public void render(DrawContext context, int x, int centerY) {
        int y = centerY - height / 2;
        Renderer.drawRect(context, x, y, width, height, 4, 0xFF15151B, 0xFFA0A0A0, .5f);
        int yOffset = y + 3;
        for (Map.Entry<String, String> entry : lines.entrySet()) {
            Renderer.drawText(context, entry.getKey(), x + 3, yOffset, 0xFFA0A0A0);
            Renderer.drawText(context, entry.getValue(), x + width - 3 - TEXT_RENDERER.getWidth(entry.getValue()), yOffset);
            yOffset += 10;
        }
    }
}