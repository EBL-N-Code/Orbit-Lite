package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.render.RenderUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TextEditor extends Widget {
    private static final int UNDO_STACK_LIMIT = 25;
    private static final long SNAPSHOT_INTERVAL_MS = 500;
    private final Deque<InputState> undoStack = new ArrayDeque<>();
    private final Deque<InputState> redoStack = new ArrayDeque<>();
    private long lastSnapshotTime = 0;
    private InputBar inputBar;
    private InputState inputState;

    public TextEditor() {
        init();
    }

    @Override
    protected void init() {
        this.inputBar = ChatScreen.getInstance().getInputBar();
        this.inputState = ChatManager.getInstance().getChat().getInputState();
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
        List<String> lines = getLines();

        CursorPosition cursor = getCursor();
        String currentLine = lines.get(cursor.line);

        int col = Math.min(cursor.column, currentLine.length());
        String before = currentLine.substring(0, col);
        String after = currentLine.substring(col);

        String firstLine = pasteLines.getFirst();

        if (pasteLines.size() == 1) {
            String newLine = before + firstLine + after;
            lines.set(cursor.line, newLine);
            setCursor(new CursorPosition(cursor.line, before.length() + firstLine.length()), false);
        } else {
            String lastLine = pasteLines.getLast();
            List<String> newLines = new ArrayList<>();

            newLines.add(before + firstLine);
            newLines.addAll(pasteLines.subList(1, pasteLines.size() - 1));
            newLines.add(lastLine + after);

            lines.remove(cursor.line);
            lines.addAll(cursor.line, newLines);

            setCursor(new CursorPosition(cursor.line + pasteLines.size() - 1, lastLine.length()), false);
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

        inputBar.update();
    }

    private void backspace() {
        CursorPosition cursor = getCursor();
        if (cursor.line == 0 && cursor.column == 0) return; // Nothing to delete
        List<String> lines = getLines();

        if (cursor.column > 0) {
            String line = lines.get(cursor.line);
            lines.set(cursor.line, line.substring(0, cursor.column - 1) + line.substring(cursor.column));
            setCursorPosition(new CursorPosition(cursor.line, cursor.column - 1));
        } else { // Merge with previous line
            String removedLine = lines.remove(cursor.line);
            cursor = new CursorPosition(cursor.line - 1, lines.get(cursor.line - 1).length());
            lines.set(cursor.line, lines.get(cursor.line) + removedLine);
            setCursorPosition(cursor);
        }
    }

    private void delete() {
        List<String> lines = getLines();
        CursorPosition cursor = getCursor();
        String line = lines.get(cursor.line);
        if (cursor.column < line.length()) {
            lines.set(cursor.line, line.substring(0, cursor.column) + line.substring(cursor.column + 1));
        } else if (cursor.line + 1 < lines.size()) {
            lines.set(cursor.line, line + lines.remove(cursor.line + 1));
        }
    }

    private void deleteSelection() {
        List<String> lines = getLines();
        CursorPosition selectionStart = getSelectionStart();
        CursorPosition selectionEnd = getSelectionEnd();
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

        setCursorPosition(start);
        setSelectionStart(start);
        setSelectionEnd(start);
    }

    private void deleteWord(int direction) {
        CursorPosition start = getCursor();
        CursorPosition end = getWordSkipPosition(direction);

        setSelectionStart((direction < 0) ? end : start);
        setSelectionEnd((direction < 0) ? start : end);
        deleteSelection();
    }

    protected CursorPosition getWordSkipPosition(int direction) {
        List<String> lines = getLines();
        CursorPosition cursor = getCursor();
        if (lines.isEmpty()) return cursor;

        int line = cursor.line;
        int column = cursor.column;
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
        List<String> lines = getLines();
        CursorPosition cursor = getCursor();

        String currentLine = lines.get(cursor.line);
        lines.set(cursor.line, currentLine.substring(0, cursor.column));
        int newLineIndex = cursor.line + 1;
        lines.add(newLineIndex, currentLine.substring(cursor.column));

        setCursor(new CursorPosition(newLineIndex, 0), false);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.enableScissor(x, y, x + width + 8, y + height - 1);

        context.getMatrices().push();
        context.getMatrices().translate(0, -inputBar.getScrollbar().getScrollOffset(), 0);

        renderText(context, x, y);
        renderCursor(context, x, y);

        context.getMatrices().pop();
        context.disableScissor();
    }

    protected void renderText(DrawContext context, int x, int y) {
        if (isEmpty()) {
            RenderUtils.drawText(context, "Ask anything", x, y, 0xA0A0A0, true);
        } else {
            CursorPosition selectionStart = getSelectionStart();
            CursorPosition selectionEnd = getSelectionEnd();
            CursorPosition selStart = CursorPosition.min(selectionStart, selectionEnd);
            CursorPosition selEnd = CursorPosition.max(selectionStart, selectionEnd);
            List<String> lines = getLines();

            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                int lineY = y + lineIndex * InputBar.LINE_HEIGHT;

                context.getMatrices().push();
                context.getMatrices().translate(0f, 0f, 5f);
                RenderUtils.drawText(context, line, x, lineY, true);
                context.getMatrices().pop();

                // Draw selection highlight if needed
                if (hasSelection() && lineIndex >= selStart.line && lineIndex <= selEnd.line) {
                    int startX = lineIndex == selStart.line ? x + textRenderer.getWidth(line.substring(0, selStart.column)) : x;
                    int endX = lineIndex == selEnd.line ? x + textRenderer.getWidth(line.substring(0, selEnd.column)) : x + textRenderer.getWidth(line);
                    if (line.isEmpty()) endX += 4;

                    context.fill(startX, lineY - 1, endX, lineY + 9, 0xFF3390FF);
                }
            }
        }
    }

    protected void renderCursor(DrawContext context, int x, int y) {
        if (isFocused() && !hasSelection()) {
            boolean blink = (Util.getMeasuringTimeMs() - inputBar.lastSwitchFocusTime) / 500L % 2L == 0L;
            if (blink) {
                List<String> lines = getLines();
                CursorPosition cursor = getCursor();
                String line = lines.get(cursor.line);
                //FIXME: error in this line (Caused by: java.lang.StringIndexOutOfBoundsException: Range [0, -12) out of bounds for length 12)
                int cursorX = x + textRenderer.getWidth(line.substring(0, Math.min(line.length(), cursor.column)));
                int cursorY = y + cursor.line * InputBar.LINE_HEIGHT;

                context.fill(RenderLayer.getGuiOverlay(), cursorX, cursorY - 1, cursorX + 1, cursorY + 9, -3092272);
            }
        }
    }

    protected void onClick(CursorPosition clickedPosition, int clickCount) {
        if (clickCount == 2) {
            selectWordAt(clickedPosition);
        } else if (clickCount >= 3) {
            String lineText = getLines().get(clickedPosition.line);
            setSelectionStart(new CursorPosition(clickedPosition.line, 0));
            CursorPosition selectionEnd = new CursorPosition(clickedPosition.line,lineText.length());
            setSelectionEnd(selectionEnd);
            setCursorPosition(selectionEnd); // Move cursor to end after triple click
        } else {
            setCursor(clickedPosition, Screen.hasShiftDown());
        }
    }

    protected String getSelection() {
        if (!hasSelection()) return "";

        CursorPosition selectionStart = getSelectionStart();
        CursorPosition selectionEnd = getSelectionEnd();
        CursorPosition start = CursorPosition.min(selectionStart, selectionEnd);
        CursorPosition end = CursorPosition.max(selectionStart, selectionEnd);
        List<String> lines = getLines();

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
        CursorPosition newPos = getCursor().offset(getLines(), direction);
        setCursor(newPos, shiftDown);
    }

    protected void moveCursorVertical(int direction, boolean shiftDown) {
        CursorPosition cursor = getCursor();
        List<String> lines = getLines();
        int newLine = MathHelper.clamp(cursor.line + direction, 0, lines.size() - 1);
        if (newLine == cursor.line) return;

        int pixelX = getCursorPixelX(cursor.line, cursor.column);
        int newColumn = findColumnForPixelX(lines.get(newLine), pixelX);
        setCursor(new CursorPosition(newLine, newColumn), shiftDown);
    }

    private int getCursorPixelX(int line, int column) {
        List<String> lines = getLines();
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
        pos = pos.clamp(getLines());
        setCursorPosition(pos);
        setSelectionEnd(pos);

        if (!shiftKeyPressed) setSelectionStart(pos);

        inputBar.update();
    }

    private void selectWordAt(CursorPosition pos) {
        List<String> lines = getLines();
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

        setSelectionStart(new CursorPosition(pos.line, start));
        CursorPosition selectionEnd = new CursorPosition(pos.line, end);
        setSelectionEnd(selectionEnd);
        setCursorPosition(selectionEnd); // cursor at the end of selection
    }

    /**
     * Wraps text lines to fit within the maximum line width, adjusting the cursor position as needed.
     */
    protected void wrapLines(int maxWidth) {
        List<String> lines = getLines();
        if (lines.isEmpty() || lines.getFirst().isEmpty()) return;

        List<String> wrappedLines = new ArrayList<>();
        CursorPosition cursor = getCursor();

        int i = 0;
        for (String line : lines) {
            List<String> list = TextUtils.wrapLines(line, maxWidth);
            if (list.isEmpty()) list.add("");

            if (list.size() > 1 && i == cursor.line) {
                String currentLine = lines.get(cursor.line);
                String after = currentLine.substring(Math.min(cursor.column, currentLine.length()));
                cursor.line += list.size() - 1;
                cursor.column = list.getLast().length() - after.length();
            }

            wrappedLines.addAll(list);
            i++;
        }

        lines.clear();
        lines.addAll(wrappedLines);
    }

    private void snapshot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSnapshotTime <= SNAPSHOT_INTERVAL_MS) return;

        InputState currentState = captureCurrentState();
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

        InputState previousState = undoStack.pop();
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

        InputState nextState = redoStack.pop();
        // Limit undo stack size
        if (undoStack.size() < UNDO_STACK_LIMIT) {
            undoStack.push(captureCurrentState());
        }
        applyState(nextState);
    }

    private InputState captureCurrentState() {
        List<String> linesCopy = new ArrayList<>(getLines());
        return new InputState(linesCopy, getCursor().copy(), getSelectionStart().copy(), getSelectionEnd().copy(), inputBar.getScrollbar().getScrollPercentage());
    }

    private void applyState(InputState state) {
        List<String> lines = getLines();
        lines.clear();
        lines.addAll(state.getLines());
        setCursorPosition(state.getCursor());
        setSelectionStart(state.getSelectionStart());
        setSelectionEnd(state.getSelectionEnd());
        inputBar.getScrollbar().setScrollPercentage(state.getScrollPercentage());
        inputBar.update();
    }

    protected void reset() {
        undoStack.clear();
        redoStack.clear();
        lastSnapshotTime = 0;
    }

    protected boolean isEmpty() {
        List<String> lines = getLines();
        return lines.size() == 1 && lines.getFirst().isEmpty();
    }

    protected boolean hasSelection() {
        return !getSelectionStart().equals(getSelectionEnd());
    }

    protected void setCursorToLineStart(boolean shiftDown) {
        setCursor(new CursorPosition(getCursor().line, 0), shiftDown);
    }

    protected void setCursorToLineEnd(boolean shiftDown) {
        CursorPosition cursor = getCursor();
        setCursor(new CursorPosition(cursor.line, getLines().get(cursor.line).length()), shiftDown);
    }

    private List<String> getLines() {
        return inputState.getLines();
    }

    private CursorPosition getSelectionStart() {
        return inputState.getSelectionStart();
    }

    private CursorPosition getSelectionEnd() {
        return inputState.getSelectionEnd();
    }

    private CursorPosition getCursor() {
        return inputState.getCursor();
    }

    private void setSelectionStart(CursorPosition cursorPosition) {
        inputState.setSelectionStart(cursorPosition);
    }

    private void setSelectionEnd(CursorPosition cursorPosition) {
        inputState.setSelectionEnd(cursorPosition);
    }

    private void setCursorPosition(CursorPosition cursorPosition) {
        inputState.setCursor(cursorPosition);
    }
}
