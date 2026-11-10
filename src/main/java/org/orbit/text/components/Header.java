package org.orbit.text.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import org.orbit.render.Renderer;
import org.orbit.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Header implements VisualLine {
    private final MutableText text;
    private final float scale;

    public Header(MutableText text, float scale) {
        this.text = text;
        this.scale = scale;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(this.scale, this.scale);
        Renderer.drawText(context, this.text, 0, 0);
        context.getMatrices().popMatrix();
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return (int) (textRenderer.fontHeight * this.scale) + 6;
    }

    public static class Parser implements BlockParser {
        private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
        private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)");

        @Override
        public boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth) {
            if (!line.contains("#")) return false;
            Matcher headerMatcher = HEADER_PATTERN.matcher(line);
            if (!headerMatcher.matches()) return false;

            int level = headerMatcher.group(1).length();
            String content = headerMatcher.group(2);
            content = content.replace("**", "");
            float scale = getScaleForLevel(level);

            List<InlineParser.StyledSegment> segments = new ArrayList<>();
            for (InlineParser.StyledSegment styledSegment : InlineParser.parse(content)) {
                segments.add(styledSegment.style().isBold() ? styledSegment : new InlineParser.StyledSegment(content, Style.EMPTY.withColor(InlineParser.BOLD_COLOR)));
            }

            List<MutableText> wrappedLines = TextWrapper.wrap(segments, TEXT_RENDERER, (int) (maxWidth / scale));
            for (MutableText textLine : wrappedLines) {
                lines.add(new Header(textLine, scale));
            }
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

        private float getScaleForLevel(int level) {
            return switch (level) {
                case 1, 2, 3 -> 1.5f;
                default -> 1f;
            };
        }
    }
}