package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.chat.Chat;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.text.StreamProcessor;

public class AIMessage extends ChatMessage {
    private final StreamProcessor streamProcessor = new StreamProcessor();
    private final StringBuilder rawContent = new StringBuilder();
    private final Chat chat = ChatManager.getInstance().getChat();
    private UsageInfo usageInfo;
    private boolean completed;

    public AIMessage() {
    }

    public AIMessage(String content) {
        rawContent.append(content);
        completed = true;
    }

    @Override
    public void calculateHeight() {
        height = streamProcessor.getHeight() + TOOLBAR_AREA_HEIGHT;
    }

    @Override
    public void init(int x, int y, int width) {
        this.calculateWidth(width);
        this.calculateHeight();
        this.x = x;
        this.y = y;
        if (completed) toolbar = new Toolbar(this);
    }

    public void update() {
        calculateHeight();

        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) {
            chatScreen.getChatDisplay().onMessageUpdate(chat);
        }
    }

    @Override
    public void calculateWidth(int displayWidth) {
        width = displayWidth;
        streamProcessor.processContent(rawContent.toString(), width);
    }

    public void render(DrawContext context) {
        streamProcessor.render(context, x, y);
    }

    public void append(String text) {
        rawContent.append(text);
        streamProcessor.processChunk(text);
        update();
    }

    public void onFinish() {
        completed = true;
        streamProcessor.finish();
        update();

        toolbar = new Toolbar(this);
    }

    @Override
    public String getContent() {
        return rawContent.toString();
    }

    public void setUsage(UsageInfo usageInfo) {
        this.usageInfo = usageInfo;
    }

    @Override
    public Sender getSender() {
        return Sender.ASSISTANT;
    }

    public UsageInfo getUsage() {
        return usageInfo;
    }

    public record UsageInfo(int inputTokens, int outputTokens, int promptTokens, int cachedInputTokens, int tokensUsage, int latency, int tps) {
    }
}