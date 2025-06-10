package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class UserMessage extends ChatMessage {

    public UserMessage(List<String> content) {
        super(content);
    }

    public UserMessage(String content, long timestamp) {
        super(content, timestamp);
    }

    @Override
    public void setWidth(int maxWidth) {
        wrapLines(maxWidth - 10);

        width = 0;
        for (String line : lines) {
            width = Math.max(width, TEXT_RENDERER.getWidth(line));
        }
        width += 10;
    }

    public void render(DrawContext context) {
        RenderUtils.drawRect(context, x, y, width, height - 12, 8, 0xFF403566);

        int y = this.y + 6;
        for (String line : lines) {
            RenderUtils.drawText(context, line, x + 5, y);
            y += 10;
        }
    }

    @Override
    public Sender getSender() {
        return Sender.USER;
    }
}
