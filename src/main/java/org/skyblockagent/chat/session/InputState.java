package org.skyblockagent.chat.session;

import org.skyblockagent.gui.inputBar.CursorPosition;

import java.util.ArrayList;
import java.util.List;

public class InputState {
    private final List<String> lines = new ArrayList<>(List.of(""));
    private CursorPosition cursorPosition = new CursorPosition(0, 0);
    private CursorPosition selectionStart = new CursorPosition(0, 0);
    private CursorPosition selectionEnd = new CursorPosition(0, 0);
    private double scrollPercentage;

    public void reset() {
        lines.clear();
        lines.add("");
        cursorPosition = new CursorPosition(0, 0);
        selectionStart = new CursorPosition(0, 0);
        selectionEnd = new CursorPosition(0, 0);
        scrollPercentage = 0d;
    }

    public List<String> getLines() {
        return lines;
    }

    public CursorPosition getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(CursorPosition cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public CursorPosition getSelectionStart() {
        return selectionStart;
    }

    public void setSelectionStart(CursorPosition selectionStart) {
        this.selectionStart = selectionStart;
    }

    public CursorPosition getSelectionEnd() {
        return selectionEnd;
    }

    public void setSelectionEnd(CursorPosition selectionEnd) {
        this.selectionEnd = selectionEnd;
    }

    public void setScrollPercentage(double scrollPercentage) {
        this.scrollPercentage = scrollPercentage;
    }
}
