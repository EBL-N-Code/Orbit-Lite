package org.skyblockagent.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.utils.render.RenderUtils;

public class Scrollbar {
    private static final int BAR_WIDTH = 3;
    private static final int MIN_HEIGHT = 8;
    private static final int OFFSET = 5;
    private final double defaultScrollPcnt;
    private final float defauttScrollOffset;
    private float x, y;
    private int minY, maxY;
    private int height;
    private boolean isVisible = false;
    private boolean isDragging = false;
    private double scrollPercentage;
    private float scrollOffset;

    public Scrollbar() {
        this(0f, 0f);
    }

    public Scrollbar(double scrollPercentage, float scrollOffset) {
        this.defaultScrollPcnt = scrollPercentage;
        this.defauttScrollOffset = scrollOffset;
        this.scrollPercentage = scrollPercentage;
        this.scrollOffset = scrollOffset;
    }

    public void init(int y, int height, int right, int offset) {
        this.x = right - BAR_WIDTH - 4;
        this.minY = y + OFFSET;
        this.maxY = y + height - OFFSET;
        updateOffset(offset, height);
    }

    public void updateOffset(int offset, int height) {
        boolean newValue = offset > height;
        if (isVisible && !newValue) reset();
        isVisible = newValue;

        if (isVisible) {
            calculateScrollOffset(offset, height);
            update(height, offset);
        }
    }

    public void calculateScrollOffset(int offset, int height) {
        float maxScroll = offset - height;
        scrollPercentage = MathHelper.clamp(scrollPercentage, 0.0f, 1.0f);
        scrollOffset = MathHelper.clamp((float) scrollPercentage * maxScroll, 0.0f, maxScroll);
    }

    public void calculateScrollPercentage(double verticalAmount, int offset, int height) {
        float scrollDelta = (float) verticalAmount * 55f / (offset - height);
        scrollPercentage = MathHelper.clamp(scrollPercentage - scrollDelta, 0.0f, 1.0f);
    }

    public void update(int viewportHeight, int contentHeight) {
        int trackActualHeight = this.maxY - this.minY;
        this.height = Math.max(MIN_HEIGHT, (int) ((double) viewportHeight / contentHeight * trackActualHeight));
        this.y = (float) (this.minY + (scrollPercentage * (trackActualHeight - this.height)));
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (!isVisible) return;

        RenderUtils.drawRect(context, x, y, BAR_WIDTH, height, 2, 0xFF4A5050);
        if (isHovered(mouseX, mouseY) || isDragging) {
            RenderUtils.drawRect(context, x, y, BAR_WIDTH, height, 2, 0x20FFFFFF);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (isHovered(mouseX, mouseY)) {
            isDragging = true;
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double verticalAmount, int offset, int height) {
        if (isVisible()) {
            calculateScrollPercentage(verticalAmount, offset, height);
            updateOffset(offset, height);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double deltaY, int yOffset, int viewHeight) {
        if (isDragging) {
            double newBarY = MathHelper.clamp(y + deltaY, minY, maxY - height);
            scrollPercentage = (newBarY - minY) / ((maxY - minY) - height);
            scrollOffset = (float) (scrollPercentage * (yOffset - viewHeight));

            update(viewHeight, yOffset);
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }

    public void reset() {
        scrollPercentage = defaultScrollPcnt;
        scrollOffset = defauttScrollOffset;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return isVisible && mouseX >= x && mouseX <= x + BAR_WIDTH && mouseY >= y && mouseY <= y + height;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisibility(boolean visible) {
        isVisible = visible;
    }

    public double getScrollPercentage() {
        return scrollPercentage;
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(float scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public void setScrollPercentage(double scrollPercentage) {
        this.scrollPercentage = scrollPercentage;
    }
}
