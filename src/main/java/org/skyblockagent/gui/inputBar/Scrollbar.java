package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

public class Scrollbar {
    private static final int CORNER_RADIUS = 2;
    private static final int TEXTURE_WIDTH = 3;
    private static final int MIN_HEIGHT = 8;
    private int x, startY;
    private int y, height;
    private boolean isVisible = false;
    private boolean isDragging = false;

    public void init(int x, int y, int width, int cornerRadius) {
        this.startY = y + cornerRadius;
        this.x = x + width - TEXTURE_WIDTH - 4;
    }

    public void update(int height, int maxScrollHeight, double scrollPercentage) {
        this.height = Math.max(MIN_HEIGHT, (int) ((float) height / maxScrollHeight * height));
        this.y = startY + (int) (scrollPercentage * (height - this.height));
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawRoundedRect(context, x, y, TEXTURE_WIDTH, height, CORNER_RADIUS, 0xFF4A5050);
        if (isHovered(mouseX, mouseY) || isDragging) {
            RenderUtils.drawRoundedRect(context, x, y, TEXTURE_WIDTH, height, CORNER_RADIUS, 0x20FFFFFF);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + TEXTURE_WIDTH && mouseY >= y && mouseY <= y + height;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    public void setVisibility(boolean visible) {
        isVisible = visible;
    }
}
