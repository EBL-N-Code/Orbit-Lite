package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.chat.Chat;
import org.skyblockagent.gui.ChatDisplay;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.text.StreamProcessor;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class AIMessage extends ChatMessage {
    private final StreamProcessor streamProcessor = new StreamProcessor();
    private final StringBuilder rawContent = new StringBuilder();
    private final List<String> errorMessageLines = new ArrayList<>();
    private Chat chat;
    private String errorMessage;
    private UsageInfo usageInfo;
    private boolean completed;

    public record UsageInfo(int completionTokens, int promptTokens, int totalTokens, int totalTime, double tps) { }

    public AIMessage(Chat chat) {
        super();
        this.chat = chat;
        chat.addAiMessage(this);
        updateDisplay();
    }

    public AIMessage(String content, long timestamp) {
        super(timestamp);
        rawContent.append(content);
        completed = true;
    }

    @Override
    public void init(int x, int y, int width) {
        this.calculateWidth(width);
        this.calculateHeight();
        this.x = x;
        this.y = y;
        if (completed) toolbar = new Toolbar(this);
    }

    @Override
    public void calculateHeight() {
        height = (error ? errorMessageLines.size() * LINE_HEIGHT : streamProcessor.getHeight());
        height += TOOLBAR_AREA_HEIGHT;
    }

    @Override
    public void calculateWidth(int displayWidth) {
        width = displayWidth;
        streamProcessor.processContent(rawContent.toString(), width);
        if (error) calculateErrorWidth();
    }

    public void render(DrawContext context) {
        if (error) {
            renderError(context);
            return;
        }
        streamProcessor.render(context, x, y);
    }

    public void renderError(DrawContext context) {
        RenderUtils.drawRect(context, x, y, width, height - (TOOLBAR_AREA_HEIGHT - 6), 6, 0x40FF5555, 1f, 0xFFFF5555);
        int lineY = y + 4;
        int lineX = x + 6;
        for (String line : errorMessageLines) {
            RenderUtils.drawText(context, line, lineX, lineY, 0xFFFF5555);
            lineY += LINE_HEIGHT;
        }
    }

    public void append(String text) {
        rawContent.append(text);
        streamProcessor.processChunk(text);
        update();
    }

    //TODO: this may have to be called from Chat not from append, onError, onFinish
    public void update() {
        int oldHeight = height;
        calculateHeight();

        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) {
            ChatDisplay chatDisplay = chatScreen.getChatDisplay();
            int diff = height - oldHeight;
            if (diff != 0) {
                chatDisplay.addSpace(diff, chat.getUuid());
            }
        }
    }

    public void onError(String error) {
        this.error = true;
        errorMessage = error;
        calculateErrorWidth();
        update();
    }

    public void calculateErrorWidth() {
        errorMessageLines.clear();
        errorMessageLines.addAll(TextUtils.wrapLines(errorMessage, width - 12));
        width = 0;
        for (String line : errorMessageLines) {
            width = Math.max(width, TEXT_RENDERER.getWidth(line));
        }
        width += 12;
    }

    public void onFinish() {
        streamProcessor.finish();
        completed = true;
        update();

        toolbar = new Toolbar(this);
    }

    @Override
    public String getContent() {
        return error ? String.join("", errorMessageLines) : rawContent.toString();
    }

    @Override
    public Sender getSender() {
        return Sender.AI;
    }

    public UsageInfo getUsage() {
        return usageInfo;
    }

    public void setUsage(int completionTokens, int promptTokens, int totalTime) {
        int totalTokens = completionTokens + promptTokens;
        double tps = (double) totalTokens / totalTime;
        this.usageInfo = new UsageInfo(completionTokens, promptTokens, totalTokens, totalTime, tps);
    }
}