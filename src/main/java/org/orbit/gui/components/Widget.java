package org.orbit.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

public abstract class Widget implements Drawable, Element, Selectable {
    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    protected int x, y, width, height;
    private boolean hovered;
    private boolean focused;
    private boolean shouldFocus = true;

    public Widget() {}

    public void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        init();
    }

    protected abstract void init();
    protected abstract void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.hovered = context.scissorContains(mouseX, mouseY)
                && mouseX >= this.x
                && mouseY >= this.y
                && mouseX < this.x + this.width
                && mouseY < this.y + this.height;
        this.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return hovered;
    }

    @Override
    public SelectionType getType() {
        if (this.isFocused()) {
            return Selectable.SelectionType.FOCUSED;
        } else {
            return this.hovered ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
        }
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void disableFocus() {
        this.shouldFocus = false;
    }

    public boolean shouldFocus() {
        boolean oldValue = shouldFocus;
        shouldFocus = true;
        return oldValue;
    }
}
