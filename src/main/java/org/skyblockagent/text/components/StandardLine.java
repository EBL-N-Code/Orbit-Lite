package org.skyblockagent.text.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.text.VisualLine;

public record StandardLine(MutableText text) implements VisualLine {

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        Renderer.drawText(context, text, x, y);
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return textRenderer.fontHeight + 4;
    }
}
