package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class UserMessage extends ChatMessage {
    private static final float MAX_WIDTH_PERCENTAGE = 2.0f / 3.0f;
    protected final List<String> lines = new ArrayList<>();
    protected List<String> content;

    public UserMessage(List<String> content) {
        this.content = content;
    }

    public UserMessage(String content) {
        this.content = List.of(content.split("\n"));
    }

    @Override
    public void init(int x, int y, int width) {
        this.calculateWidth((int) (width * MAX_WIDTH_PERCENTAGE));
        this.calculateHeight();
        this.x = x + width - this.width;
        this.y = y;
        toolbar = new Toolbar(this);
    }

    @Override
    public void calculateHeight() {
        this.height = lines.size() * LINE_HEIGHT + TOOLBAR_AREA_HEIGHT;
    }

    @Override
    public void calculateWidth(int maxWidth) {
        lines.clear();
        width = 0;
        for (String line : content) {
            List<String> wrappedLines = TextUtils.wrapLines(line, maxWidth - 10);
            for (String wrappedLine : wrappedLines) {
                width = Math.max(width, TEXT_RENDERER.getWidth(wrappedLine));
                lines.add(wrappedLine);
            }
        }
        width += 10;
    }

    public void render(DrawContext context) {
        Renderer.drawRect(context, x, y, width, height - 12, 8, 0xFF403566);
        int lineY = y + 6;
        int lineX = x + 5;
        for (String line : lines) {
            Renderer.drawText(context, line, lineX, lineY);
            lineY += 10;
        }
    }

    @Override
    public String getContent() {
        return String.join("\n", content);
    }

    @Override
    public Sender getSender() {
        return Sender.USER;
    }
}
