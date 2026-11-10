package org.orbit.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.orbit.render.Renderer;
import org.orbit.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ErrorMessage extends ChatMessage {
    private final List<String> errorLines = new ArrayList<>();
    private final String error;

    public ErrorMessage(String error) {
        this.error = error;
    }

    @Override
    public void init(int x, int y, int width) {
        this.calculateWidth(width);
        this.calculateHeight();
        this.x = x;
        this.y = y;
        this.toolbar = new Toolbar(this);
    }

    @Override
    public void render(DrawContext context) {
        Renderer.drawRect(context, x, y, width, height - (TOOLBAR_AREA_HEIGHT - 6), 6, 0x40FF5555, 0xFFFF5555, 1f);
        int lineY = y + 4;
        int lineX = x + 6;
        for (String line : errorLines) {
            Renderer.drawText(context, line, lineX, lineY, 0xFFFF5555);
            lineY += LINE_HEIGHT;
        }
    }

    @Override
    public void calculateWidth(int displayWidth) {
        errorLines.clear();
        errorLines.addAll(TextUtils.wrapLines(error, displayWidth - 12));
        width = 0;
        for (String line : errorLines) {
            width = Math.max(width, TEXT_RENDERER.getWidth(line));
        }
        width += 12;
    }

    @Override
    public void calculateHeight() {
        height = (errorLines.size() * LINE_HEIGHT) + TOOLBAR_AREA_HEIGHT;
    }

    @Override
    public String getContent() {
        return error;
    }

    @Override
    public Sender getSender() {
        return Sender.ASSISTANT;
    }
}
