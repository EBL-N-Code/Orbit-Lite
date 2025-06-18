package org.skyblockagent.text;

import java.util.List;

public interface BlockParser {
    boolean canStart(String line, List<VisualLine> lines, String lastLine, int lastLineCount, int maxWidth);

    ParseResult continueParsing(String line, List<VisualLine> lines, int maxWidth);

    default boolean isInstant() {
        return false;
    }
}