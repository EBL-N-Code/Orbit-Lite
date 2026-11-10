package org.orbit.gui.inputBar;

import net.minecraft.util.math.MathHelper;

import java.util.List;

public class CursorPosition {
    public int line;
    public int column;

    public CursorPosition() {
        this.line = 0;
        this.column = 0;
    }

    public CursorPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public static CursorPosition min(CursorPosition a, CursorPosition b) {
        if (a.line != b.line) return a.line < b.line ? a : b;
        return a.column <= b.column ? a : b;
    }

    public static CursorPosition max(CursorPosition a, CursorPosition b) {
        if (a.line != b.line) return a.line > b.line ? a : b;
        return a.column >= b.column ? a : b;
    }

    public static CursorPosition fromAbsoluteIndex(List<String> lines, int idx) {
        int line = 0;
        while (line < lines.size()) {
            int len = lines.get(line).length();
            if (idx <= len) break;
            idx -= len + 1;
            line++;
        }
        return new CursorPosition(line, idx);
    }

    public static int getTotalLength(List<String> lines) {
        int total = 0;
        for (String line : lines) {
            total += line.length() + 1;
        }
        return Math.max(0, total - 1); // No '\n' after last line
    }

    public CursorPosition copy() {
        return new CursorPosition(line, column);
    }

    public CursorPosition offset(List<String> lines, int delta) {
        if (lines.isEmpty()) return new CursorPosition();

        int idx = getAbsoluteIndex(lines);
        idx = MathHelper.clamp(idx + delta, 0, getTotalLength(lines));
        return fromAbsoluteIndex(lines, idx);
    }

    public int getAbsoluteIndex(List<String> lines) {
        int idx = 0;
        for (int i = 0; i < line; i++) {
            idx += lines.get(i).length() + 1; // +1 for '\n'
        }
        return idx + column;
    }

    public CursorPosition clamp(List<String> lines) {
        if (lines.isEmpty()) return new CursorPosition();

        int lineClamped = MathHelper.clamp(this.line, 0, lines.size() - 1);
        int colClamped = MathHelper.clamp(this.column, 0, lines.get(lineClamped).length());
        return line == lineClamped && column == colClamped ? this : new CursorPosition(lineClamped, colClamped);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CursorPosition pos)) return false;
        return pos.line == line && pos.column == column;
    }
}