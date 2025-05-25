package org.skyblockagent.managers.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.skyblockagent.gui.ChatScreen;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatMessage {
    final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    final List<String> rawContent;
    final List<String> wrappedContent = new ArrayList<>();
    final long timestamp;
    int x, y, width, height;
    String content;

    public ChatMessage(List<String> rawContent) {
        this.rawContent = rawContent;
        this.timestamp = System.currentTimeMillis();
        ChatScreen.INSTANCE.getChatDisplay().onMessage(this);
    }

    public void init(int x, int y, int displayWidth, int maxWidth) {
        this.setWidth(getSender() == MessageSender.AI ? displayWidth : maxWidth);
        this.x = getSender() == MessageSender.AI ? x : x + displayWidth - width;
        this.y = y;
        this.height = wrappedContent.size() * 10 + 10;
    }

    public void update() {
        wrapLines(width);
        this.height = wrappedContent.size() * 10 + 10;
    }

    public abstract MessageSender getSender();

    public abstract void setWidth(int width);

    public void wrapLines(int maxWidth) {
        wrappedContent.clear();

        for (String line : rawContent) {
            for (StringVisitable str : textRenderer.getTextHandler().wrapLines(line, maxWidth, Style.EMPTY)) {
                wrappedContent.add(str.getString());
            }
        }
    }

    public abstract void render(DrawContext context);

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
