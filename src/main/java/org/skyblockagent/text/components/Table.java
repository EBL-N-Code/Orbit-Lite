package org.skyblockagent.text.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Table implements VisualLine {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final int CELL_PADDING = 4;
    private final List<MutableText> headers = new ArrayList<>();
    private final List<List<MutableText>> bodyRows = new ArrayList<>();
    private final List<Alignment> alignments = new ArrayList<>();
    private final List<Integer> columnWidths = new ArrayList<>();
    private final List<Integer> rowHeights = new ArrayList<>();
    private final List<List<MutableText>> wrappedHeaderText = new ArrayList<>();
    private final List<List<List<MutableText>>> wrappedBodyRows = new ArrayList<>();
    private final int outerMaxWidth;
    private int totalHeight, totalWidth;

    public Table(int maxWidth) {
        this.outerMaxWidth = maxWidth;
    }

    public void setHeaders(List<String> rawHeaders) {
        this.headers.addAll(rawHeaders.stream()
                .map(h -> InlineParser.parse(h).stream()
                        .map(seg -> Text.literal(seg.text()).setStyle(seg.style()))
                        .reduce(Text.empty(), MutableText::append))
                .toList());
        recalculateLayout();
    }

    public void addRow(List<String> rawCells) {
        this.bodyRows.add(rawCells.stream()
                .map(cell -> InlineParser.parse(cell).stream()
                        .map(seg -> Text.literal(seg.text()).setStyle(seg.style()))
                        .reduce(Text.empty(), MutableText::append))
                .collect(Collectors.toList()));
        recalculateLayout();
    }

    public void setAlignments(List<Alignment> alignments) {
        this.alignments.addAll(alignments);
    }

    /**
     * The core layout engine. This is called whenever the table's data changes.
     */
    private void recalculateLayout() {
        int numColumns = headers.size();
        if (numColumns == 0) return;

        // --- Pass 1: Calculate Ideal Column Widths (based on unwrapped content) ---
        List<Integer> idealWidths = new ArrayList<>(Collections.nCopies(numColumns, 0));
        for (int i = 0; i < numColumns; i++) {
            idealWidths.set(i, TEXT_RENDERER.getWidth(headers.get(i)));
        }
        for (List<MutableText> row : bodyRows) {
            for (int i = 0; i < row.size() && i < numColumns; i++) {
                idealWidths.set(i, Math.max(idealWidths.get(i), TEXT_RENDERER.getWidth(row.get(i))));
            }
        }

        // --- Pass 2: Allocate Constrained Widths ---
        columnWidths.clear();
        int requiredWidth = idealWidths.stream().mapToInt(w -> w).sum() + (CELL_PADDING * 2 * numColumns) + numColumns - 1;
        if (requiredWidth <= outerMaxWidth) {
            columnWidths.addAll(idealWidths);
        } else {
            double shrinkFactor = (double) (outerMaxWidth - (CELL_PADDING * 2 * numColumns) - numColumns + 1) / idealWidths.stream().mapToInt(w -> w).sum();
            for (int idealWidth : idealWidths) {
                columnWidths.add(Math.max(10, (int) (idealWidth * shrinkFactor))); // Ensure a minimum column width
            }
        }

        // --- Pass 3: Wrap Text and Calculate Row Heights and Widths ---
        wrappedHeaderText.clear();
        wrappedBodyRows.clear();
        rowHeights.clear();

        // Header
        int headerMaxLines = 1;
        for (int i = 0; i < numColumns; i++) {
            List<MutableText> wrappedHeader = TextWrapper.wrap(headers.get(i), TEXT_RENDERER, columnWidths.get(i));
            wrappedHeaderText.add(wrappedHeader);
            headerMaxLines = Math.max(headerMaxLines, wrappedHeader.size());
        }
        rowHeights.add(headerMaxLines * TEXT_RENDERER.fontHeight);

        // Body Rows
        for (List<MutableText> row : bodyRows) {
            int rowMaxLines = 1;
            List<List<MutableText>> wrappedRow = new ArrayList<>();
            for (int i = 0; i < row.size() && i < numColumns; i++) {
                List<MutableText> wrappedCell = TextWrapper.wrap(row.get(i), TEXT_RENDERER, columnWidths.get(i));
                wrappedRow.add(wrappedCell);
                rowMaxLines = Math.max(rowMaxLines, wrappedCell.size());
            }
            wrappedBodyRows.add(wrappedRow);
            rowHeights.add(rowMaxLines * TEXT_RENDERER.fontHeight);
        }

        // Final total height and width calculation
        this.totalHeight = rowHeights.stream().mapToInt(h -> h).sum() + (CELL_PADDING * 2 * (bodyRows.size() + 1)) + 1;
        this.totalWidth = columnWidths.stream().mapToInt(w -> w).sum() + (CELL_PADDING * 2 * columnWidths.size());
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y) {
        if (headers.isEmpty() || columnWidths.isEmpty()) return;
        int currentY = y;

        // Draw Header
        renderRow(context, textRenderer, wrappedHeaderText, x, currentY);
        currentY += rowHeights.getFirst() + CELL_PADDING * 2;

        // Draw Separator
        context.drawHorizontalLine(x, x + totalWidth, currentY - CELL_PADDING, 0x80424242);

        // Draw Body Rows
        for (int i = 0; i < wrappedBodyRows.size(); i++) {
            renderRow(context, textRenderer, wrappedBodyRows.get(i), x, currentY);
            currentY += rowHeights.get(i + 1) + CELL_PADDING * 2;
            if (i + 1 < wrappedBodyRows.size()) context.drawHorizontalLine(x, x + totalWidth, currentY - 1, 0x802C2C2C);
        }
    }

    private void renderRow(DrawContext context, TextRenderer textRenderer, List<List<MutableText>> wrappedRowData, int x, int y) {
        int currentX = x;
        for (int i = 0; i < wrappedRowData.size(); i++) {
            int contentWidth = columnWidths.get(i);
            List<MutableText> wrappedCell = wrappedRowData.get(i);
            Alignment alignment = (i < this.alignments.size()) ? this.alignments.get(i) : Alignment.LEFT;

            int lineY = y + CELL_PADDING;
            for (MutableText line : wrappedCell) {
                int textWidth = textRenderer.getWidth(line);
                int textX = switch (alignment) {
                    case CENTER -> currentX + CELL_PADDING + (contentWidth - textWidth) / 2;
                    case RIGHT -> currentX + CELL_PADDING + contentWidth - textWidth;
                    default -> currentX + CELL_PADDING;
                };

                Renderer.drawText(context, line, textX, lineY);
                lineY += textRenderer.fontHeight;
            }
            currentX += contentWidth + CELL_PADDING * 2;
        }
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return totalHeight;
    }

    public enum Alignment {LEFT, CENTER, RIGHT}

    public static class Parser implements BlockParser {
        private static final Pattern SEPARATOR_PATTERN = Pattern.compile("^\\s*\\|?.*[-:]+.*\\|?\\s*$");
        private boolean inTable = false;

        @Override
        public boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth) {
            if (lines.size() < 2 || !SEPARATOR_PATTERN.matcher(line).matches()) return false;
            if (lastLine.contains("|") && lines.getLast() instanceof StandardLine) {
                if (lastLineCount <= lines.size()) lines.subList(lines.size() - lastLineCount, lines.size()).clear();

                Table table = new Table(maxWidth);
                table.setHeaders(parseRow(lastLine));
                table.setAlignments(parseAlignments(line));

                lines.add(table);
                inTable = true;
                return true;
            }
            return false;
        }

        @Override
        public ParseResult continueParsing(String line, List<VisualLine> lines, int maxWidth) {
            if (!this.inTable) return ParseResult.REJECTED;

            if (!line.contains("|")) {
                this.inTable = false;
                return ParseResult.REJECTED;
            }

            Table table = (Table) lines.getLast();
            table.addRow(parseRow(line));

            return ParseResult.CONTINUE;
        }

        private List<String> parseRow(String line) {
            String content = line.trim();
            if (content.startsWith("|")) content = content.substring(1);
            if (content.endsWith("|")) content = content.substring(0, content.length() - 1);
            return Arrays.stream(content.split("\\|")).map(String::trim).collect(Collectors.toList());
        }

        private List<Alignment> parseAlignments(String line) {
            List<String> parts = parseRow(line);
            List<Alignment> alignments = new ArrayList<>();
            for (String part : parts) {
                boolean left = part.startsWith(":");
                boolean right = part.endsWith(":");
                if (left && right) alignments.add(Alignment.CENTER);
                else if (right) alignments.add(Alignment.RIGHT);
                else alignments.add(Alignment.LEFT); // Default
            }
            return alignments;
        }
    }
}