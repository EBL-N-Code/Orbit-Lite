package org.skyblockagent.text.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.skyblockagent.text.BlockParser;
import org.skyblockagent.text.ParseResult;
import org.skyblockagent.text.VisualLine;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock implements VisualLine {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final int PADDING = 4;
    private final List<MutableText> lines = new ArrayList<>();
    private final int width;
    private int height = PADDING * 2;

    public CodeBlock(int width) {
        this.width = width;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        RenderUtils.drawRect(context, x, y, width, height, 6, 0x80242629);

        int lineY = y + PADDING;
        for (MutableText line : lines) {
            RenderUtils.drawText(context, line, x + PADDING, lineY);
            lineY += textRenderer.fontHeight;
        }
    }

    public void appendLine(MutableText line) {
        lines.add(line);
        height += TEXT_RENDERER.fontHeight;
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return height;
    }

    public static class Parser implements BlockParser {
        private boolean inCodeBlock = false;

        @Override
        public boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth) {
            if (line.trim().startsWith("```")) {
                this.inCodeBlock = true;
                return true;
            }
            return false;
        }

        @Override
        public ParseResult continueParsing(String line, List<VisualLine> lines, int maxWidth) {
            if (!this.inCodeBlock) return ParseResult.REJECTED;
            if (line.trim().startsWith("```")) {
                this.inCodeBlock = false;
                return ParseResult.FINISH;
            }

            CodeBlock block;
            if (!lines.isEmpty() && lines.getLast() instanceof CodeBlock b) {
                block = b;
            } else {
                block = new CodeBlock(maxWidth);
                lines.add(block);
            }

            for (String wrappedLine : TextUtils.wrapLines(line, maxWidth - PADDING * 2)) {
                block.appendLine(Text.literal(wrappedLine));
            }

            return ParseResult.CONTINUE;
        }
    }
}