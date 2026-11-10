package org.orbit.gui.components.dropdown;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SeparatorOption implements DropdownItem {
    private static final int HEIGHT = 5;

    @Override
    public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
        int lineY = y + HEIGHT / 2;
        int lineX = x + PADDING / 2;
        context.fill(lineX, lineY, lineX + width - PADDING, lineY + 1, 0xFF404040);
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 0;
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
    }
}
