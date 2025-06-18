package org.skyblockagent.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.ChatScreen;

public abstract class ChatMessage {
    protected static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    protected static final int LINE_HEIGHT = 10;
    protected static final int TOOLBAR_AREA_HEIGHT = 22;
    protected final long timestamp;
    protected int x, y, width, height;
    protected boolean error;
    protected Toolbar toolbar;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(long timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void init(int x, int y, int width);
    public abstract void render(DrawContext context);
    public abstract void calculateWidth(int width);
    public abstract void calculateHeight();
    public abstract String getContent();
    public abstract Sender getSender();

    public void updateDisplay() {
        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) chatScreen.getChatDisplay().onMessage(this);
    }

    public boolean isHovered(double mouseX, double mouseY, float scrollOffset) {
        float effectiveY = y - scrollOffset;
        return mouseX >= x && mouseX <= x + width && mouseY >= effectiveY && mouseY <= effectiveY + height;
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

    public long getTimestamp() {
        return timestamp;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public boolean isError() {
        return error;
    }
}
