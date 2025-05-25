package org.skyblockagent.managers.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.ChatDisplay;
import org.skyblockagent.gui.ChatScreen;

import java.util.List;

public class AIMessage extends ChatMessage {
    public AIMessage(List<String> content) {
        super(content);
    }

    @Override
    public void setWidth(int displayWidth) {
        wrapLines(displayWidth);
        this.width = displayWidth;
    }

    public void render(DrawContext context) {
        int yOffset = 0;
        for (String line : wrappedContent) {
            context.drawText(textRenderer, line, x, y + yOffset, 0xFFFFFFFF, true);
            yOffset += 10;
        }
    }

    public void onFinish() {
        this.content = String.join("", rawContent);
    }

    public void add(String msg) {
        rawContent.set(rawContent.size() - 1, rawContent.getLast() + msg);

        int oldHeight = height;
        update();

        ChatDisplay chatDisplay = ChatScreen.INSTANCE.getChatDisplay();
        if (chatDisplay != null) {
            int diff = height - oldHeight;
            if (diff > 0) {
                chatDisplay.addSpace(diff);
                chatDisplay.updateScrollBar();
            }
        }
    }

    @Override
    public MessageSender getSender() {
        return MessageSender.AI;
    }
}
