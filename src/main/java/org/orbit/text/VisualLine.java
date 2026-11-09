package org.apex.text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface VisualLine {
    /**
     * Renders this line to the screen.
     */
    void render(DrawContext context, TextRenderer textRenderer, int x, int y);

    /**
     * Returns the total height of this line in pixels.
     */
    int getHeight(TextRenderer textRenderer);
}