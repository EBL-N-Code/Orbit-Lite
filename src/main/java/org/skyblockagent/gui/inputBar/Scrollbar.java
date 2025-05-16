package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

public class Scrollbar {
    private static final int CORNER_RADIUS = 2;
    private static final int BAR_WIDTH = 3;
    private static final int MIN_HEIGHT = 8;
    private int x, y;
    private int minY, maxY;
    private int height;
    private boolean isVisible = false;
    private boolean isDragging = false;

    public void init(int minY, int maxY, int right) {
        this.x = right - BAR_WIDTH - 4;
        this.minY = minY;
        this.maxY = maxY;
    }

    public void update(int viewportHeight, int contentHeight, double scrollPercentage) {
        int trackActualHeight = this.maxY - this.minY;
        this.height = Math.max(MIN_HEIGHT, (int) ((double) viewportHeight / contentHeight * trackActualHeight));
        this.y = this.minY + (int) (scrollPercentage * (trackActualHeight - this.height));
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (!isVisible) return;

        RenderUtils.drawRoundedRect(context, x, y, BAR_WIDTH, height, CORNER_RADIUS, 0xFF4A5050);
        if (isHovered(mouseX, mouseY) || isDragging) {
            RenderUtils.drawRoundedRect(context, x, y, BAR_WIDTH, height, CORNER_RADIUS, 0x20FFFFFF);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return isVisible && mouseX >= x && mouseX <= x + BAR_WIDTH && mouseY >= y && mouseY <= y + height;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
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

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}
