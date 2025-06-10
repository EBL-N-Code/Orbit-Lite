package org.skyblockagent.gui.inputBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputState {
    private final List<String> lines = new ArrayList<>(List.of(""));
    private CursorPosition cursor = new CursorPosition();
    private CursorPosition selectionStart = new CursorPosition();
    private CursorPosition selectionEnd = new CursorPosition();
    private double scrollPercentage;

    public InputState() {}

    public InputState(List<String> lines, CursorPosition cursor, CursorPosition selectionStart, CursorPosition selectionEnd, double scrollPercentage) {
        this.lines.clear();
        this.lines.addAll(lines);
        this.cursor = cursor;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.scrollPercentage = scrollPercentage;
    }

    public void reset() {
        lines.clear();
        lines.add("");
        cursor = new CursorPosition();
        selectionStart = new CursorPosition();
        selectionEnd = new CursorPosition();
        scrollPercentage = 0d;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InputState that)) return false;
        // Compare sizes first to avoid deep comparison
        if (lines.size() != that.lines.size() || !cursor.equals(that.cursor) ||
                !selectionStart.equals(that.selectionStart) || !selectionEnd.equals(that.selectionEnd)) {
            return false;
        }
        // Compare lines content
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).equals(that.lines.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines.size(), cursor, selectionStart, selectionEnd);
    }

    public List<String> getLines() {
        return lines;
    }

    public CursorPosition getCursor() {
        return cursor;
    }

    public void setCursor(CursorPosition cursor) {
        this.cursor = cursor;
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

    public double getScrollPercentage() {
        return scrollPercentage;
    }

    public void setScrollPercentage(double scrollPercentage) {
        this.scrollPercentage = scrollPercentage;
    }
}
