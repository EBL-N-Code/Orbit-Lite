package org.orbit.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import org.orbit.text.components.*;

import java.util.ArrayList;
import java.util.List;

public class StreamProcessor {
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final List<BlockParser> blockParsers;
    private final StringBuilder incomingBuffer = new StringBuilder();
    private final StringBuilder currentLineBuffer = new StringBuilder();
    private final List<VisualLine> visualLines = new ArrayList<>();
    private final List<VisualLine> liveLines = new ArrayList<>();
    private String lastLine = "";
    private BlockParser activeBlockParser = null;
    private int lastLineCount;
    private int maxWidth, height;

    static {
        blockParsers = List.of(
                new Header.Parser(),
                new HorizontalRule.Parser(),
                new CodeBlock.Parser(),
                new Blockquote.Parser(),
                new Table.Parser()
        );
    }

    public void processChunk(String chunk) {
        incomingBuffer.append(chunk.replace("\t", "    "));

        int newlineIndex;
        while ((newlineIndex = incomingBuffer.indexOf("\n")) != -1) {
            String linePart = incomingBuffer.substring(0, newlineIndex).replace("\r", "");
            currentLineBuffer.append(linePart);
            finalizeCurrentLine();
            incomingBuffer.delete(0, newlineIndex + 1);
        }

        if (!incomingBuffer.isEmpty()) {
            currentLineBuffer.append(incomingBuffer);
            incomingBuffer.setLength(0);
            updateLivePreview();
        }

        calculateHeight();
    }

    private void handleLine(String rawLine, List<VisualLine> targetList) {
        String trimmed = rawLine.trim();
        if (trimmed.length() > 1 && trimmed.charAt(1) == ' ' && List.of('*', '+', '-').contains(trimmed.charAt(0))) {
            int i = 0;
            while (i < rawLine.length() && Character.isWhitespace(rawLine.charAt(i))) {
                i++;
            }

            String indent = rawLine.substring(0, i);
            String content = rawLine.substring(i + 2);
            rawLine = indent + "• " + content;
        }

        List<InlineParser.StyledSegment> styledSegments = InlineParser.parse(rawLine);
        List<MutableText> wrappedTextLines = TextWrapper.wrap(styledSegments, textRenderer, maxWidth);

        for (MutableText textLine : wrappedTextLines) {
            targetList.add(new StandardLine(textLine));
        }
    }

    /**
     * This is the core dispatcher. It processes a completed line of text
     * by either feeding it to an active block parser or finding a new one.
     */
    private void finalizeCurrentLine() {
        String line = currentLineBuffer.toString();
        boolean blockHandled = false;

        if (activeBlockParser != null) {
            ParseResult parseResult = activeBlockParser.continueParsing(line, this.visualLines, this.maxWidth);
            activeBlockParser = parseResult != ParseResult.CONTINUE ? null : activeBlockParser;
            blockHandled = parseResult != ParseResult.REJECTED;
        } else {
            for (BlockParser parser : blockParsers) {
                if (parser.canStart(line, this.visualLines, lastLine, this.lastLineCount, this.maxWidth)) {
                    activeBlockParser = !parser.isInstant() ? parser : null;
                    blockHandled = true;
                    break;
                }
            }
        }

        int vLinesSize = visualLines.size();
        if (!blockHandled) handleLine(line, this.visualLines);
        lastLine = line;
        lastLineCount = visualLines.size() - vLinesSize;
        currentLineBuffer.setLength(0);
        liveLines.clear();
    }

    /**
     * The current line has been updated. Reparse it and update the live preview.
     */
    private void updateLivePreview() {
        liveLines.clear();
        handleLine(currentLineBuffer.toString(), this.liveLines);
    }

    public void finish() {
        // If there's any leftover text in the incoming buffer, process it
        if (!incomingBuffer.isEmpty()) {
            currentLineBuffer.append(incomingBuffer);
            incomingBuffer.setLength(0);
        }

        // Finalize any text that was part of the last line
        if (!currentLineBuffer.isEmpty()) {
            finalizeCurrentLine();
        }
        calculateHeight();
    }

    public void processContent(String content, int maxWidth) {
        this.maxWidth = maxWidth;
        if (content.isEmpty()) return;
        incomingBuffer.setLength(0);
        currentLineBuffer.setLength(0);
        visualLines.clear();
        liveLines.clear();
        lastLine = "";
        activeBlockParser = null;
        lastLineCount = 0;
        processChunk(content);
    }

    public void render(DrawContext context, int x, int y) {
        for (VisualLine visualLine : visualLines) {
            visualLine.render(context, textRenderer, x, y);
            y += visualLine.getHeight(textRenderer);
        }

        for (VisualLine visualLine : liveLines) {
            visualLine.render(context, textRenderer, x, y);
            y += visualLine.getHeight(textRenderer);
        }
    }

    public void calculateHeight() {
        height = 0;
        for (VisualLine visualLine : visualLines) {
            height += visualLine.getHeight(textRenderer);
        }

        for (VisualLine visualLine : liveLines) {
            height += visualLine.getHeight(textRenderer);
        }
    }

    public int getHeight() {
        return height;
    }
}
