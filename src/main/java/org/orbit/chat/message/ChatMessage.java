package org.apex.chat.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class ChatMessage {
    protected static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    protected static final int LINE_HEIGHT = 10;
    protected static final int TOOLBAR_AREA_HEIGHT = 22;
    protected int x, y, width, height;
    protected Toolbar toolbar;

    public abstract void init(int x, int y, int width);
    public abstract void render(DrawContext context);

    public abstract void calculateWidth(int displayWidth);
    public abstract void calculateHeight();
    public abstract String getContent();
    public abstract Sender getSender();

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

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

    public Toolbar getToolbar() {
        return toolbar;
    }
}
