package org.skyblockagent.managers.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class UserMessage extends ChatMessage {

    public UserMessage(List<String> content) {
        super(content);
        this.content = String.join("", rawContent);
    }

    public UserMessage(List<String> rawContent, long timestamp) {
        super(rawContent, timestamp);
    }

    @Override
    public void setWidth(int maxWidth) {
        wrapLines(maxWidth - 10);

        width = 0;
        for (String line : wrappedContent) {
            width = Math.max(width, textRenderer.getWidth(line));
        }
        width += 10;
    }

    public void render(DrawContext context) {
        renderBackground(context);

        int yOffset = 0;
        for (String line : wrappedContent) {
            context.drawText(textRenderer, line, x + 5, y + yOffset + 6, 0xFFFFFFFF, true);
            yOffset += 10;
        }
    }

    public void renderBackground(DrawContext context) {
        RenderUtils.drawRect(context, x, y, width, height, 8, 0xFF403566);
    }

    @Override
    public MessageSender getSender() {
        return MessageSender.USER;
    }
}
