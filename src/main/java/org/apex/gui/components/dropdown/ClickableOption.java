package org.apex.gui.components.dropdown;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.apex.render.Renderer;

public class ClickableOption implements DropdownItem {
    protected final String label;
    protected final Runnable action;

    public ClickableOption(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
        if (isHovered) {
            Renderer.drawRect(context, x + OFFSET, y, width - (OFFSET * 2), HEIGHT, 4, 0x33FFFFFF);
        }

        Renderer.drawText(
                context,
                label,
                x + PADDING,
                y + 1 + (HEIGHT - textRenderer.fontHeight) / 2
        );
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return textRenderer.getWidth(label) + (PADDING * 2);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void onClick() {
        if (action != null) {
            action.run();
        }
    }
}
