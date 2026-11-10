package org.orbit.text.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.orbit.text.BlockParser;
import org.orbit.text.ParseResult;
import org.orbit.text.VisualLine;

import java.util.List;
import java.util.regex.Pattern;

public class HorizontalRule implements VisualLine {
    private final int padding = 2;
    private final int width;

    public HorizontalRule(int width) {
        this.width = width;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        context.drawHorizontalLine(x, x + width, y + padding, 0xFF808080);
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 1 + padding * 2;
    }

    public static class Parser implements BlockParser {
        private static final Pattern HR_PATTERN = Pattern.compile("^\\s*([-_*+=~])(?:\\s*\\1){2,}\\s*$");

        @Override
        public boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth) {
            if (!HR_PATTERN.matcher(line.trim()).matches()) return false;
            lines.add(new HorizontalRule(maxWidth));
            return true;
        }

        @Override
        public ParseResult continueParsing(String line, List<VisualLine> lines, int maxWidth) {
            return ParseResult.REJECTED;
        }

        @Override
        public boolean isInstant() {
            return true;
        }
    }
}