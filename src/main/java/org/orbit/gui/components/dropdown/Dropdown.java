package org.orbit.gui.components.dropdown;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.text.Text;
import org.orbit.gui.components.Widget;
import org.orbit.render.Renderer;

import java.util.ArrayList;
import java.util.List;

public class Dropdown extends Widget {
    private static final int MIN_WIDTH = 50;
    private static final int OFFSET = 3;
    private final String identifier;
    private final List<DropdownItem> items;
    private DropdownItem hoveredItem = null;

    protected Dropdown(Builder builder) {
        this.identifier = builder.identifier;
        this.items = builder.items;
        this.width = calculateTotalWidth();
        this.height = calculateTotalHeight();

        int initialX = builder.centerX ? builder.x - (width / 2) : builder.x;
        int windowWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        this.x = Math.max(0, Math.min(initialX, windowWidth - width));
        this.y = builder.y;
    }

    public static Builder builder(String identifier) {
        return new Builder(identifier);
    }

    @Override
    protected void init() {
    }

    private int calculateTotalWidth() {
        int maxWidth = MIN_WIDTH;
        for (DropdownItem item : items) {
            int itemWidth = item.getWidth(textRenderer);
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
        return maxWidth;
    }

    private int calculateTotalHeight() {
        int total = OFFSET * 2;
        for (DropdownItem item : items) {
            total += item.getHeight();
        }
        return total;
    }

    public boolean handleClick() {
        if (hoveredItem != null && hoveredItem.isClickable()) {
            hoveredItem.onClick();
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Renderer.drawRect(context, x, y, width, height, 4, 0xFF15151B, 0xFF404040, .5f);

        int currentY = y + OFFSET;
        hoveredItem = null;

        for (DropdownItem item : items) {
            int itemHeight = item.getHeight();

            boolean isHovered = item.isClickable() &&
                    mouseX >= x + OFFSET && mouseX < x + width - OFFSET &&
                    mouseY >= currentY && mouseY < currentY + itemHeight;

            if (isHovered) {
                hoveredItem = item;
                context.setCursor(StandardCursors.POINTING_HAND);
            }

            item.render(context, x, currentY, width, isHovered, textRenderer);
            currentY += itemHeight;
        }
    }

    public boolean isHovered(double mouseX, double mouseY, float scrollOffset) {
        return isHovered(mouseX, mouseY + scrollOffset);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static class Builder {
        private final String identifier;
        private final List<DropdownItem> items = new ArrayList<>();
        private int x;
        private int y;
        private boolean centerX;

        private Builder(String identifier) {
            this.identifier = identifier;
        }

        public Builder position(int x, int y, boolean centerX) {
            this.centerX = centerX;
            return position(x, y);
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder addClickableOption(ClickableOption clickableOption) {
            this.items.add(clickableOption);
            return this;
        }

        public Builder addOption(String label, Runnable action) {
            this.items.add(new ClickableOption(label, action));
            return this;
        }

        public Builder addText(Text text) {
            this.items.add(new TextOption(text));
            return this;
        }

        public Builder addSeparator() {
            this.items.add(new SeparatorOption());
            return this;
        }

        public Dropdown build() {
            return new Dropdown(this);
        }
    }
}
