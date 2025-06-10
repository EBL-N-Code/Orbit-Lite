package org.skyblockagent.chat.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatMessage {
    protected static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    protected final List<String> content;
    protected final List<String> lines = new ArrayList<>();
    protected final long timestamp;
    protected int x, y, width, height;
    protected boolean error;
    protected Toolbar toolbar;

    public ChatMessage(List<String> content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) chatScreen.getChatDisplay().onMessage(this);
    }

    public ChatMessage(String content, long timestamp) {
        this.content = List.of(content.split("\n"));
        this.timestamp = timestamp;
    }

    public void init(int x, int y, int displayWidth, int maxWidth, boolean initToolbar) {
        Sender sender = getSender();
        boolean isAI = sender == Sender.AI;
        this.setWidth(isAI ? displayWidth : maxWidth);
        this.setHeight();
        this.x = isAI ? x : x + displayWidth - width;
        this.y = y;
        if (initToolbar) toolbar = new Toolbar(this);
    }

    public void setHeight() {
        this.height = lines.size() * 10 + 22;
    }

    public abstract void render(DrawContext context);
    public abstract void setWidth(int width);
    public abstract Sender getSender();

    public void wrapLines(int maxWidth) {
        lines.clear();

        for (String line : content) {
            List<String> list = TextUtils.wrapLines(line, maxWidth);
            lines.addAll(list);
        }
    }

    public boolean isHovered(double mouseX, double mouseY, float scrollOffset) {
        return mouseX <= x + width && mouseY >= y - scrollOffset && mouseY <= y + height - scrollOffset;
    }

    public String getContent() {
        return String.join("\n", content);
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
