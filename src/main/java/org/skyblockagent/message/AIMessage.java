package org.skyblockagent.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatDisplay;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.network.RunInfo;
import org.skyblockagent.text.StreamProcessor;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AIMessage extends ChatMessage {
    private static final int ERROR_BACKGROUND_COLOR = 0x40FF5555;
    private static final int ERROR_BORDER_COLOR = 0xFFFF5555;
    private static final int ERROR_TEXT_COLOR = 0xFFFF5555;
    private final StreamProcessor streamProcessor = new StreamProcessor();
    private final StringBuilder rawContent = new StringBuilder();
    private final List<String> errorMessageLines = new ArrayList<>();
    private final UUID chatId = ChatManager.getInstance().getChat().getId();
    private String errorMessage;
    private RunInfo runInfo;
    private boolean completed;

    public AIMessage() {
        super();
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
        if (completed || error) height += TOOLBAR_AREA_HEIGHT;
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
        RenderUtils.drawRect(context, x, y, width, height - (TOOLBAR_AREA_HEIGHT - 6), 6, ERROR_BACKGROUND_COLOR, 1f, ERROR_BORDER_COLOR);
        int lineY = y + 4;
        int lineX = x + 6;
        for (String line : errorMessageLines) {
            RenderUtils.drawText(context, line, lineX, lineY, ERROR_TEXT_COLOR);
            lineY += LINE_HEIGHT;
        }
    }

    public void append(String text) {
        rawContent.append(text);
        streamProcessor.processChunk(text);
        update();
    }

    public void update() {
        int oldHeight = height;
        calculateHeight();

        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) {
            ChatDisplay chatDisplay = chatScreen.getChatDisplay();
            int diff = height - oldHeight;
            if (diff != 0) {
                chatDisplay.addSpace(diff, chatId);
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

    public RunInfo getRunInfo() {
        return runInfo;
    }

    public void setRunInfo(RunInfo runInfo) {
        this.runInfo = runInfo;
    }
}