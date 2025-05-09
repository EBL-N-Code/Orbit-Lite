package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TextEditor {
    private static final int UNDO_STACK_LIMIT = 25;
    private static final long SNAPSHOT_INTERVAL_MS = 500;
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final InputBar inputBar;
    private final int maxLineWidth;
    private final List<String> lines = new ArrayList<>(List.of(""));
    private final Deque<EditorState> undoStack = new ArrayDeque<>();
    private final Deque<EditorState> redoStack = new ArrayDeque<>();
    private CursorPosition cursorPosition = new CursorPosition(0, 0);
    private CursorPosition selectionStart = new CursorPosition(0, 0);
    private CursorPosition selectionEnd = new CursorPosition(0, 0);
    private long lastSnapshotTime = 0;
    private boolean focused = true;
    private int x, y;
    private int width, height;

    protected TextEditor(InputBar inputBar, int maxLineWidth) {
        this.inputBar = inputBar;
        this.maxLineWidth = maxLineWidth;
    }

    protected void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Inserts the specified text at the cursor position, replacing any selected text.
     *
     * @param text The text to insert.
     */
    protected void insertText(String text) {
        if (text == null || text.isEmpty()) return;
        snapshot();

        if (hasSelection()) deleteSelection();

        List<String> pasteLines = text.lines().toList();
        String currentLine = lines.get(cursorPosition.line);
        int col = Math.min(cursorPosition.column, currentLine.length());
        String before = currentLine.substring(0, col);
        String after = currentLine.substring(col);

        String firstLine = pasteLines.getFirst();

        if (pasteLines.size() == 1) {
            String newLine = before + firstLine + after;
            lines.set(cursorPosition.line, newLine);
            setCursor(new CursorPosition(cursorPosition.line, before.length() + firstLine.length()), false);
        } else {
            String lastLine = pasteLines.getLast();
            List<String> newLines = new ArrayList<>();

            newLines.add(before + firstLine);
            newLines.addAll(pasteLines.subList(1, pasteLines.size() - 1));
            newLines.add(lastLine + after);

            lines.remove(cursorPosition.line);
            lines.addAll(cursorPosition.line, newLines);

            setCursor(new CursorPosition(cursorPosition.line + pasteLines.size() - 1, lastLine.length()), false);
        }
    }

    /**
     * Deletes text in the specified direction, optionally deleting an entire word if control is pressed.
     *
     * @param direction   The direction to delete (-1 for backspace, 1 for delete).
     * @param controlDown True if the control key is pressed, enabling word deletion.
     */
    protected void erase(int direction, boolean controlDown) {
        snapshot();

        if (hasSelection()) {
            deleteSelection();
        } else if (controlDown) {
            deleteWord(direction);
        } else {
            if (direction < 0) {
                backspace();
            } else {
                delete();
            }
        }

        inputBar.refreshLayout(true);
    }

    private void backspace() {
        if (cursorPosition.line == 0 && cursorPosition.column == 0) return; // Nothing to delete

        if (cursorPosition.column > 0) {
            String line = lines.get(cursorPosition.line);
            lines.set(cursorPosition.line, line.substring(0, cursorPosition.column - 1) + line.substring(cursorPosition.column));
            cursorPosition = new CursorPosition(cursorPosition.line, cursorPosition.column - 1);
        } else { // Merge with previous line
            String removedLine = lines.remove(cursorPosition.line);
            cursorPosition = new CursorPosition(cursorPosition.line - 1, lines.get(cursorPosition.line - 1).length());
            lines.set(cursorPosition.line, lines.get(cursorPosition.line) + removedLine);
        }
    }

    private void delete() {
        String line = lines.get(cursorPosition.line);
        if (cursorPosition.column < line.length()) {
            lines.set(cursorPosition.line, line.substring(0, cursorPosition.column) + line.substring(cursorPosition.column + 1));
        } else if (cursorPosition.line + 1 < lines.size()) {
            lines.set(cursorPosition.line, line + lines.remove(cursorPosition.line + 1));
        }
    }

    private void deleteSelection() {
        CursorPosition start = CursorPosition.min(selectionStart, selectionEnd);
        CursorPosition end = CursorPosition.max(selectionStart, selectionEnd);

        if (start.line == end.line) {
            // Single line selection
            String line = lines.get(start.line);
            lines.set(start.line, line.substring(0, start.column) + line.substring(end.column));
        } else {
            // Multi-line selection
            String mergedLine = lines.get(start.line).substring(0, start.column)
                    + lines.get(end.line).substring(end.column);
            lines.subList(start.line, end.line + 1).clear();
            lines.add(start.line, mergedLine);
        }

        cursorPosition = start;
        selectionStart = start;
        selectionEnd = start;
    }

    private void deleteWord(int direction) {
        CursorPosition start = cursorPosition;
        CursorPosition end = getWordSkipPosition(direction);

        selectionStart = (direction < 0) ? end : start;
        selectionEnd = (direction < 0) ? start : end;
        deleteSelection();
    }

    protected CursorPosition getWordSkipPosition(int direction) {
        if (lines.isEmpty()) return cursorPosition;

        int line = cursorPosition.line;
        int column = cursorPosition.column;
        String text = lines.get(line);

        if (direction > 0) { // Move right
            if (column == text.length()) {
                return (line + 1 < lines.size()) ? new CursorPosition(line + 1, 0) : new CursorPosition(line, column);
            }

            // Move through word characters
            while (column < text.length() && !Character.isWhitespace(text.charAt(column))) {
                column++;
            }
            // Move through whitespace
            while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
                column++;
            }
        } else { // Move left
            // If at start of line, go to end of previous line
            if (column == 0) {
                return (line > 0) ? new CursorPosition(line - 1, lines.get(line - 1).length()) : new CursorPosition(line, column);
            }

            // Move through whitespace
            while (column > 0 && Character.isWhitespace(text.charAt(column - 1))) {
                column--;
            }
            // Move through word characters
            while (column > 0 && !Character.isWhitespace(text.charAt(column - 1))) {
                column--;
            }
        }
        return new CursorPosition(line, column);
    }

    protected void insertNewLine() {
        snapshot();

        String currentLine = lines.get(cursorPosition.line);
        lines.set(cursorPosition.line, currentLine.substring(0, cursorPosition.column));
        lines.add(cursorPosition.line + 1, currentLine.substring(cursorPosition.column));

        setCursor(new CursorPosition(cursorPosition.line + 1, 0), false);
    }

    protected void render(DrawContext context, float scrollOffset) {
        context.enableScissor(x, y, x + width, y + height - 1);

        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        renderText(context, x, y);
        renderCursor(context, x, y);

        context.getMatrices().pop();
        context.disableScissor();
    }

    protected void renderText(DrawContext context, int x, int y) {
        if (isEmpty()) {
            context.drawText(textRenderer, "Ask anything", x, y, 0xA0A0A0, true);
        } else {
            CursorPosition selStart = CursorPosition.min(selectionStart, selectionEnd);
            CursorPosition selEnd = CursorPosition.max(selectionStart, selectionEnd);

            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                int lineY = y + lineIndex * InputBar.LINE_HEIGHT;

                context.getMatrices().push();
                context.getMatrices().translate(0f, 0f, 5f);
                context.drawText(textRenderer, line, x, lineY, 0xFFFFFF, true);
                context.getMatrices().pop();

                // Draw selection highlight if needed
                if (hasSelection() && lineIndex >= selStart.line && lineIndex <= selEnd.line) {
                    int startX = lineIndex == selStart.line ? x + textRenderer.getWidth(line.substring(0, selStart.column)) : x;
                    int endX = lineIndex == selEnd.line ? x + textRenderer.getWidth(line.substring(0, selEnd.column)) : x + textRenderer.getWidth(line);

                    context.fill(startX, lineY - 1, endX, lineY + 9, 0xFF3390FF);
                }
            }
        }
    }

    protected void renderCursor(DrawContext context, int x, int y) {
        if (focused && !hasSelection()) {
            boolean blink = (Util.getMeasuringTimeMs() - inputBar.lastSwitchFocusTime) / 500L % 2L == 0L;
            if (blink) {
                String line = lines.get(cursorPosition.line);
                int cursorX = x + textRenderer.getWidth(line.substring(0, Math.min(line.length(), cursorPosition.column)));
                int cursorY = y + cursorPosition.line * InputBar.LINE_HEIGHT;

                context.fill(RenderLayer.getGuiOverlay(), cursorX, cursorY - 1, cursorX + 1, cursorY + 9, -3092272);
            }
        }
    }

    protected void onClick(CursorPosition clickedPosition, int clickCount) {
        if (clickCount == 2) {
            selectWordAt(clickedPosition);
        } else if (clickCount >= 3) {
            String lineText = lines.get(clickedPosition.line);
            setSelectionStart(new CursorPosition(clickedPosition.line, 0));
            setSelectionEnd(new CursorPosition(clickedPosition.line, lineText.length()));
            setCursorPosition(selectionEnd); // Move cursor to end after triple click
        } else {
            setCursor(clickedPosition, Screen.hasShiftDown());
        }
    }

    protected String getSelection() {
        if (!hasSelection()) {
            return "";
        }

        CursorPosition start = CursorPosition.min(selectionStart, selectionEnd);
        CursorPosition end = CursorPosition.max(selectionStart, selectionEnd);

        if (start.line == end.line) {
            return lines.get(start.line).substring(start.column, end.column);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lines.get(start.line).substring(start.column)).append("\n");

        for (int i = start.line + 1; i < end.line; i++) {
            sb.append(lines.get(i)).append("\n");
        }

        sb.append(lines.get(end.line), 0, end.column);
        return sb.toString();
    }

    /**
     * Moves the cursor by the specified number of characters, optionally extending the selection.
     *
     * @param direction The number of characters to move (positive for right, negative for left).
     * @param shiftDown True if the shift key is pressed, extending the selection.
     */
    protected void moveCursor(int direction, boolean shiftDown) {
        CursorPosition newPos = cursorPosition.offset(lines, direction);
        setCursor(newPos, shiftDown);
    }

    protected void moveCursorVertical(int direction, boolean shiftDown) {
        int newLine = MathHelper.clamp(cursorPosition.line + direction, 0, lines.size() - 1);
        if (newLine == cursorPosition.line) return;

        int pixelX = getCursorPixelX(cursorPosition.line, cursorPosition.column);
        int newColumn = findColumnForPixelX(lines.get(newLine), pixelX);
        setCursor(new CursorPosition(newLine, newColumn), shiftDown);
    }

    private int getCursorPixelX(int line, int column) {
        if (line < 0 || line >= lines.size()) return 0;

        String text = lines.get(line);
        if (column < 0) column = 0;
        if (column > text.length()) column = text.length();

        return textRenderer.getWidth(text.substring(0, column));
    }

    protected int findColumnForPixelX(String text, int pixelX) {
        if (text.isEmpty()) return 0;

        int x = 0;
        for (int i = 0; i < text.length(); i++) {
            int charWidth = textRenderer.getWidth(String.valueOf(text.charAt(i)));
            if (pixelX < x + charWidth / 2) return i;
            x += charWidth;
        }
        return text.length();
    }

    /**
     * Sets the cursor position, optionally extending the selection if shift is pressed.
     *
     * @param pos             The new cursor position.
     * @param shiftKeyPressed True if the shift key is pressed, extending the selection.
     */
    protected void setCursor(CursorPosition pos, boolean shiftKeyPressed) {
        pos = pos.clamp(lines);
        cursorPosition = pos;
        selectionEnd = pos;

        if (!shiftKeyPressed) selectionStart = pos;

        inputBar.refreshLayout(true);
        inputBar.lastSwitchFocusTime = Util.getMeasuringTimeMs();
    }

    private void selectWordAt(CursorPosition pos) {
        if (lines.isEmpty()) return;
        pos = pos.clamp(lines);

        String lineText = lines.get(pos.line);
        if (lineText.isEmpty() || pos.column < 0 || pos.column > lineText.length()) return;

        int start = pos.column;
        int end = pos.column;

        while (start > 0 && !Character.isWhitespace(lineText.charAt(start - 1))) {
            start--;
        }
        while (end < lineText.length() && !Character.isWhitespace(lineText.charAt(end))) {
            end++;
        }

        selectionStart = new CursorPosition(pos.line, start);
        selectionEnd = new CursorPosition(pos.line, end);

        cursorPosition = selectionEnd; // cursor at the end of selection
    }

    /**
     * Wraps text lines to fit within the maximum line width, adjusting the cursor position as needed.
     */
    protected void wrapLines() {
        List<String> wrappedLines = new ArrayList<>();

        int i = 0;
        for (String line : lines) {
            List<StringVisitable> list = textRenderer.getTextHandler().wrapLines(line, maxLineWidth, Style.EMPTY);
            if (list.isEmpty()) list.add(StringVisitable.plain(""));

            if (list.size() > 1 && i == cursorPosition.line) {
                String currentLine = lines.get(cursorPosition.line);
                String after = currentLine.substring(Math.min(cursorPosition.column, currentLine.length()));
                cursorPosition.line += list.size() - 1;
                cursorPosition.column = list.getLast().getString().length() - after.length();
            }

            for (StringVisitable stringVisitable : list) {
                wrappedLines.add(stringVisitable.getString());
            }

            i++;
        }

        lines.clear();
        lines.addAll(wrappedLines);
    }

    private void snapshot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSnapshotTime <= SNAPSHOT_INTERVAL_MS) return;

        EditorState currentState = captureCurrentState();
        if (!undoStack.isEmpty() && undoStack.peek().equals(currentState)) return;

        // Limit stack size
        if (undoStack.size() >= UNDO_STACK_LIMIT) undoStack.removeLast(); // Remove oldest state
        undoStack.push(currentState);
        redoStack.clear();
        lastSnapshotTime = currentTime;
    }

    /**
     * Reverts the text editor to the previous state in the undo stack, if available.
     */
    protected void undo() {
        if (undoStack.isEmpty()) return;

        EditorState previousState = undoStack.pop();
        // Limit redo stack size
        if (redoStack.size() < UNDO_STACK_LIMIT) {
            redoStack.push(captureCurrentState());
        }
        applyState(previousState);
    }

    /**
     * Restores the next state in the redo stack, if available.
     */
    protected void redo() {
        if (redoStack.isEmpty()) return;

        EditorState nextState = redoStack.pop();
        // Limit undo stack size
        if (undoStack.size() < UNDO_STACK_LIMIT) {
            undoStack.push(captureCurrentState());
        }
        applyState(nextState);
    }

    private EditorState captureCurrentState() {
        List<String> linesCopy = new ArrayList<>(lines);
        return new EditorState(linesCopy, cursorPosition.copy(), selectionStart.copy(), selectionEnd.copy());
    }

    private void applyState(EditorState state) {
        lines.clear();
        lines.addAll(state.lines());
        cursorPosition = state.cursor();
        selectionStart = state.selectionStart();
        selectionEnd = state.selectionEnd();
        inputBar.refreshLayout(true);
    }

    protected boolean isEmpty() {
        return lines.size() == 1 && lines.getFirst().isEmpty();
    }

    protected boolean hasSelection() {
        return !selectionStart.equals(selectionEnd);
    }

    protected void setCursorToLineStart(boolean shiftDown) {
        setCursor(new CursorPosition(cursorPosition.line, 0), shiftDown);
    }

    protected void setCursorToLineEnd(boolean shiftDown) {
        setCursor(new CursorPosition(cursorPosition.line, lines.get(cursorPosition.line).length()), shiftDown);
    }

    protected void setSelectionEnd(CursorPosition selectionEnd) {
        this.selectionEnd = selectionEnd;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x - 4 && mouseX <= x + width + 4 && mouseY >= y - 2 && mouseY <= y + height + 2;
    }

    protected void setSelectionStart(CursorPosition selectionStart) {
        this.selectionStart = selectionStart;
    }

    protected List<String> getLines() {
        return lines;
    }

    protected CursorPosition getCursorPosition() {
        return cursorPosition;
    }

    protected void setCursorPosition(CursorPosition cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    protected int getY() {
        return y;
    }

    protected boolean isFocused() {
        return focused;
    }

    protected void setFocused(boolean focused) {
        this.focused = focused;
    }

    protected int getHeight() {
        return height;
    }
}
