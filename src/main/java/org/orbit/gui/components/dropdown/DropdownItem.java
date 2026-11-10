package org.orbit.gui.components.dropdown;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface DropdownItem {
    int HEIGHT = 14;
    int OFFSET = 3;
    int PADDING = 8;

    void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer);

    int getWidth(TextRenderer textRenderer);

    int getHeight();

    boolean isClickable();

    void onClick();
}
