package org.skyblockagent.gui.inputBar;

import java.util.List;
import java.util.Objects;

public record EditorState(List<String> lines, CursorPosition cursor, CursorPosition selectionStart, CursorPosition selectionEnd) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditorState that)) return false;
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
}