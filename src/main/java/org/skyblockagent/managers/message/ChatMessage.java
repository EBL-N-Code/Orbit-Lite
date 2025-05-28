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
    final List<String> content;
    final List<String> wrappedContent = new ArrayList<>();
    final long timestamp;
    int x, y, width, height;

    public ChatMessage(List<String> content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        ChatScreen.INSTANCE.getChatDisplay().onMessage(this);
    }

    public ChatMessage(String content, long timestamp) {
        this.content = List.of(content.split("\n"));
        this.timestamp = timestamp;
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

    public abstract void render(DrawContext context);
    public abstract MessageSender getSender();
    public abstract void setWidth(int width);

    public void wrapLines(int maxWidth) {
        wrappedContent.clear();

        for (String line : content) {
            for (StringVisitable str : textRenderer.getTextHandler().wrapLines(line, maxWidth, Style.EMPTY)) {
                wrappedContent.add(str.getString());
            }
        }
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

    public int getHeight() {
        return height;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
