package org.skyblockagent.managers.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatMessage {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final List<String> content;
    private final List<String> renderableLines = new ArrayList<>();
    private final long timestamp;
    private int x, y, width, height;

    public ChatMessage(List<String> content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public void setDimentions(int maxWidth) {
        wrapLines(maxWidth);

        width = 0;
        for (String line : renderableLines) {
            width = Math.max(width, textRenderer.getWidth(line));
        }

        this.width += 10;
        this.height = renderableLines.size() * 10 + 10;
    }

    public void wrapLines(int maxWidth) {
        renderableLines.clear();

        for (String line : content) {
            List<StringVisitable> stringVisitables = textRenderer.getTextHandler().wrapLines(line, maxWidth - 10, Style.EMPTY);
            for (StringVisitable stringVisitable : stringVisitables) {
                renderableLines.add(stringVisitable.getString());
            }
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(DrawContext context) {
        renderBackground(context);

        int yOffset = 0;
        for (String line : renderableLines) {
            context.drawText(textRenderer, line, x + 5, y + yOffset + 6, 0xFFFFFFFF, true);
            yOffset += 10;
        }
    }

    public void renderBackground(DrawContext context) {
        RenderUtils.drawRoundedRect(context, x, y, width, height, 8, 0xFF1B1C1D, 0.5f, 0xFF424447);
    }

    public abstract MessageSender getSender();

    public List<String> getContent() {
        return content;
    }

    public List<String> getRenderableLines() {
        return renderableLines;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
