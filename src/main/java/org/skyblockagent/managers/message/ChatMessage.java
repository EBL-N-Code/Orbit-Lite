package org.skyblockagent.managers.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Uuids;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public ChatMessage(List<String> rawContent, long timestamp) {
        this.rawContent = rawContent;
        this.timestamp = timestamp;
    }

    // Codec for ChatMessage (handles both UserMessage and AIMessage)
    public static final Codec<ChatMessage> CHAT_MESSAGE_CODEC = Codec.STRING.dispatch(
            "type", // Field to determine the type
            msg -> msg instanceof UserMessage ? "user" : "ai", // Serialize type
            type -> switch (type) {
                case "user" -> RecordCodecBuilder.<UserMessage>mapCodec(instance ->
                        instance.group(
                                Codec.STRING.listOf().fieldOf("content").forGetter(ChatMessage::getRawContent),
                                Codec.LONG.fieldOf("timestamp").forGetter(ChatMessage::getTimestamp)
                        ).apply(instance, UserMessage::new)
                );
                case "ai" -> RecordCodecBuilder.<AIMessage>mapCodec(instance ->
                        instance.group(
                                Codec.STRING.listOf().fieldOf("content").forGetter(ChatMessage::getRawContent),
                                Codec.LONG.fieldOf("timestamp").forGetter(ChatMessage::getTimestamp)
                        ).apply(instance, AIMessage::new)
                );
                default -> throw new IllegalArgumentException("Unknown message type: " + type);
            }
    );

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

        for (String line : rawContent) {
            for (StringVisitable str : textRenderer.getTextHandler().wrapLines(line, maxWidth, Style.EMPTY)) {
                wrappedContent.add(str.getString());
            }
        }
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

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getRawContent() {
        return rawContent;
    }
}
