package org.apex.text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextWrapper {
    private static final Pattern TOKENIZER = Pattern.compile("\\S+|\\s+");
    private static final Style EMPTY_STYLE = Style.EMPTY;

    /**
     * Wraps a styled text into multiple lines, correctly calculating the width of
     * styled text (bold, italic, etc.) to ensure proper wrapping.
     */
    public static List<MutableText> wrap(List<InlineParser.StyledSegment> segments, TextRenderer textRenderer, int maxWidth) {
        List<MutableText> wrappedLines = new ArrayList<>();
        MutableText currentLine = Text.empty();
        int currentWidth = 0;

        for (InlineParser.StyledSegment segment : segments) {
            Matcher matcher = TOKENIZER.matcher(segment.text());

            while (matcher.find()) {
                String tokenString = matcher.group();
                MutableText styledToken = Text.literal(tokenString).setStyle(segment.style());
                int tokenWidth = textRenderer.getWidth(styledToken);
                boolean isWord = !tokenString.trim().isEmpty();

                if (isWord && currentWidth != 0 && currentWidth + tokenWidth > maxWidth) {
                    wrappedLines.add(currentLine);
                    currentLine = Text.empty();
                    currentWidth = 0;
                }

                if (isWord && tokenWidth > maxWidth && currentWidth == 0) {
                    currentLine = wrapLongToken(styledToken, wrappedLines, textRenderer, maxWidth);
                    currentWidth = textRenderer.getWidth(currentLine);
                    continue;
                }

                if (currentWidth + tokenWidth <= maxWidth) {
                    currentLine.append(styledToken);
                    currentWidth += tokenWidth;
                } else if (isWord) {
                    wrappedLines.add(currentLine);
                    currentLine = styledToken;
                    currentWidth = tokenWidth;
                }
            }
        }

        if (!currentLine.getString().isEmpty()) wrappedLines.add(currentLine);
        if (wrappedLines.isEmpty()) wrappedLines.add(Text.empty());
        return wrappedLines;
    }

    /**
     * A helper method to wrap a single, very long token character by character.
     *
     * @param longToken The styled token that is too wide.
     * @param lines     The list of finalized lines to add full lines to.
     * @param renderer  The TextRenderer for measurements.
     * @param maxWidth  The maximum width of a line.
     * @return The final, partially filled line after wrapping the long token.
     */
    private static MutableText wrapLongToken(MutableText longToken, List<MutableText> lines, TextRenderer renderer, int maxWidth) {
        StringBuilder part = new StringBuilder();
        int partWidth = 0;

        for (char c : longToken.getString().toCharArray()) {
            int charWidth = renderer.getWidth(String.valueOf(c));

            if (partWidth + charWidth > maxWidth) {
                lines.add(Text.literal(part.toString()).setStyle(longToken.getStyle()));
                part.setLength(0);
                partWidth = 0;
            }

            part.append(c);
            partWidth += charWidth;
        }

        return Text.literal(part.toString()).setStyle(longToken.getStyle());
    }

    public static List<MutableText> wrap(MutableText text, TextRenderer textRenderer, int maxWidth) {
        List<InlineParser.StyledSegment> segments = new ArrayList<>();
        text.visit((style, string) -> {
            segments.add(new InlineParser.StyledSegment(string, style));
            return Optional.empty();
        }, EMPTY_STYLE);

        return wrap(segments, textRenderer, maxWidth);
    }
}