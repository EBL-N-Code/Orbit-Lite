package org.skyblockagent.chat.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TextParser {
    private final StringBuilder buffer = new StringBuilder();
    private final List<TextLine> lines = new ArrayList<>();
    private TextLine currentLine;
    private int maxWidth;

    public void processChunk(String chunk) {
        buffer.append(chunk);
        parse();
    }

    private void parse() {
        if (currentLine == null) appendLine();
        int lastProcessedIndex = 0;
        int newlineIndex;

        while ((newlineIndex = buffer.indexOf("\n", lastProcessedIndex)) != -1) {
            String line = buffer.substring(lastProcessedIndex, newlineIndex);
            currentLine.appendText(line);
            appendLine();
            lastProcessedIndex = newlineIndex + 1;
        }

        if (lastProcessedIndex > 0) buffer.delete(0, lastProcessedIndex);
        if (!buffer.isEmpty()) currentLine.appendText(buffer.toString());
    }

    public void appendLine() {
        currentLine = new TextLine(maxWidth);
        lines.add(currentLine);
    }

    public void render(DrawContext context, int x, int y) {
        for (TextLine line : lines) {
            for (MutableText mutableText : line.getText()) {
                RenderUtils.drawText(context, mutableText, x, y, RenderUtils.WHITE_COLOR, false);
                y += 10;
            }
        }
    }

    public List<TextLine> getLines() {
        return lines;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}