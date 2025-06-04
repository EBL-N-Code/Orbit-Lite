package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.ChatDisplay;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class AIMessage extends ChatMessage {

    public AIMessage(List<String> content) {
        super(content);
    }

    public AIMessage(String content, long timestamp) {
        super(content, timestamp);
    }

    @Override
    public void setWidth(int displayWidth) {
        wrapLines(displayWidth);
        this.width = displayWidth;
    }

    public void render(DrawContext context) {
        int yOffset = 0;
        for (String line : wrappedContent) {
            RenderUtils.drawText(context, line, x, y + yOffset);
            yOffset += 10;
        }
    }

    public void add(String msg) {
        content.set(content.size() - 1, content.getLast() + msg);

        int oldHeight = height;
        update();

        ChatDisplay chatDisplay = ChatScreen.INSTANCE.getChatDisplay();
        if (chatDisplay != null) {
            int diff = height - oldHeight;
            if (diff > 0) {
                chatDisplay.addSpace(diff);
            }
        }
    }

    @Override
    public MessageSender getSender() {
        return MessageSender.AI;
    }
}
