package org.skyblockagent.text.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import org.skyblockagent.text.VisualLine;
import org.skyblockagent.utils.render.RenderUtils;

public record StandardLine(MutableText text) implements VisualLine {

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        RenderUtils.drawText(context, text, x, y);
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return textRenderer.fontHeight;
    }
}
