package org.skyblockagent.gui.components.dropdown;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.skyblockagent.render.Renderer;

public record TextOption(Text text) implements DropdownItem {

    @Override
    public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
        Renderer.drawText(
                context,
                (MutableText) text,
                x + PADDING,
                y + 1 + (HEIGHT - textRenderer.fontHeight) / 2
        );
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return textRenderer.getWidth(text) + (PADDING * 2);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public void onClick() {
        // No action for static text
    }
}
