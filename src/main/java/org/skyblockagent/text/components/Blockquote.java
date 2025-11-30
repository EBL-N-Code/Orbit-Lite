package org.skyblockagent.text.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.text.*;

import java.util.ArrayList;
import java.util.List;

public class Blockquote implements VisualLine {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final int BAR_WIDTH = 2;
    private static final int PADDING_LEFT = 4;
    private final List<MutableText> lines = new ArrayList<>();
    private int height;

    public void appendLine(MutableText line) {
        lines.add(line);
        height += TEXT_RENDERER.fontHeight;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        Renderer.drawRect(context, x, y - 2, BAR_WIDTH, height + 1, 1, 0xFF434343);

        int lineY = y;
        int textX = x + BAR_WIDTH + PADDING_LEFT;
        for (MutableText line : lines) {
            Renderer.drawText(context, line, textX, lineY);
            lineY += textRenderer.fontHeight;
        }
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return height;
    }

    public static class Parser implements BlockParser {
        @Override
        public boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth) {
            return continueParsing(line, lines, maxWidth) == ParseResult.CONTINUE;
        }

        @Override
        public ParseResult continueParsing(String line, List<VisualLine> lines, int maxWidth) {
            if (!line.startsWith("> ")) return ParseResult.REJECTED;

            Blockquote block;
            if (!lines.isEmpty() && lines.getLast() instanceof Blockquote b) {
                block = b;
            } else {
                block = new Blockquote();
                lines.add(block);
            }

            String quote = line.substring(2);
            List<InlineParser.StyledSegment> styledSegments = InlineParser.parse(quote);
            List<MutableText> wrappedTextLines = TextWrapper.wrap(styledSegments, TEXT_RENDERER, maxWidth - (BAR_WIDTH + PADDING_LEFT));

            for (MutableText wrappedTextLine : wrappedTextLines) {
                block.appendLine(wrappedTextLine);
            }

            return ParseResult.CONTINUE;
        }
    }
}