package org.apex.text;

import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class InlineParser {
    public static final int BOLD_COLOR = 0xFFFFFFFF;
    private static final Style EMPTY_STYLE = Style.EMPTY;
    private static final Style BOLD_STYLE = EMPTY_STYLE.withColor(BOLD_COLOR);
    private static final Style ITALIC_STYLE = EMPTY_STYLE.withItalic(true);
    private static final Style ITALIC_BOLD_STYLE = ITALIC_STYLE.withColor(BOLD_COLOR);
    private static final Style STRIKETHROUGH_STYLE = EMPTY_STYLE.withStrikethrough(true);
    private static final Style INLINE_STYLE = EMPTY_STYLE.withColor(0xFF676767);

    /**
     * The main entry point. Orchestrates the three-phase parsing process.
     */
    public static List<StyledSegment> parse(String text) {
        if (text == null || text.isEmpty()) return Collections.singletonList(new StyledSegment("", EMPTY_STYLE));

        List<Delimiter> delimiters = scanForDelimiters(text);
        List<MatchedPair> pairs = processDelimiters(delimiters);
        return reconstructSegments(text, pairs);
    }

    /**
     * PHASE 1: Scan the text and create a list of all potential delimiters.
     * This phase gathers the evidence without making any decisions.
     */
    private static List<Delimiter> scanForDelimiters(String text) {
        List<Delimiter> delimiters = new ArrayList<>();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != '*' && c != '_' && c != '~' && c != '`') continue;

            // Find how long the run of this character is (throwable.g., * vs ** vs ***)
            int runStart = i;
            while (i + 1 < chars.length && chars[i + 1] == c) {
                i++;
            }

            // Determine context (whitespace before/after) for open/close rules
            char charBefore = runStart > 0 ? chars[runStart - 1] : ' ';
            char charAfter = i < chars.length - 1 ? chars[i + 1] : ' ';

            boolean canOpen = !Character.isWhitespace(charAfter);
            boolean canClose = !Character.isWhitespace(charBefore);

            // Special markdown rule: `_` cannot open/close within a word if surrounded by alphanumerics
            if (c == '_' && Character.isLetterOrDigit(charBefore) && Character.isLetterOrDigit(charAfter)) {
                canOpen = canClose = false;
            }

            delimiters.add(new Delimiter(runStart, i - runStart + 1, c, canOpen, canClose));
        }
        return delimiters;
    }

    /**
     * PHASE 2: Process Delimiters
     */
    private static List<MatchedPair> processDelimiters(List<Delimiter> delimiters) {
        List<MatchedPair> pairs = new ArrayList<>();
        // The stack holds the INDEX of the delimiter in the original list.
        Stack<Integer> openerStack = new Stack<>();

        for (int i = 0; i < delimiters.size(); i++) {
            Delimiter current = delimiters.get(i);

            // A delimiter can act as a closer if its flag is set.
            if (current.canClose) {
                // Search backwards through the stack for the most recent compatible opener.
                int openerStackIndex = -1;
                for (int j = openerStack.size() - 1; j >= 0; j--) {
                    Delimiter opener = delimiters.get(openerStack.get(j));
                    // Simple matching rule: same type and same length.
                    if (opener.type == current.type && opener.length == current.length) {
                        openerStackIndex = j;
                        break;
                    }
                }

                if (openerStackIndex != -1) {
                    Delimiter opener = delimiters.get(openerStack.get(openerStackIndex));
                    pairs.add(new MatchedPair(getStyle(opener.type, opener.length), opener, current));
                    // Remove the matched opener from the stack.
                    openerStack.remove(openerStackIndex);
                    // This delimiter has been used as a closer, so it cannot also be an opener.
                    continue;
                }
            }

            // If it wasn't used as a closer, and it can be an opener, push it to the stack.
            if (current.canOpen) openerStack.push(i);
        }
        return pairs;
    }

    private static Style getStyle(char type, int length) {
        if (type == '`') return INLINE_STYLE;
        if (type == '~' && length == 2) return STRIKETHROUGH_STYLE;
        if (type == '*' || type == '_') {
            return switch (length) {
                case 1 -> ITALIC_STYLE;
                case 2 -> BOLD_STYLE;
                case 3 -> ITALIC_BOLD_STYLE;
                default -> EMPTY_STYLE;
            };
        }
        return EMPTY_STYLE;
    }

    /**
     * PHASE 3: Reconstruct Segments.
     * This method is correct and remains unchanged. A small bug in the pop logic is fixed.
     */
    private static List<StyledSegment> reconstructSegments(String text, List<MatchedPair> pairs) {
        List<Event> events = new ArrayList<>();
        for (MatchedPair pair : pairs) {
            events.add(new Event(pair.openIndex, EventType.OPEN, pair.style, pair.openLength));
            events.add(new Event(pair.closeIndex, EventType.CLOSE, pair.style, pair.closeLength));
        }
        Collections.sort(events);

        List<StyledSegment> segments = new ArrayList<>();
        Stack<Style> styleStack = new Stack<>();
        styleStack.push(EMPTY_STYLE);
        int lastIndex = 0;

        for (Event event : events) {
            if (event.index > lastIndex) {
                segments.add(new StyledSegment(text.substring(lastIndex, event.index), styleStack.peek()));
            }

            if (event.type == EventType.OPEN) {
                styleStack.push(styleStack.peek().withParent(event.style));
            } else if (styleStack.size() > 1) {
                styleStack.pop();
            }
            lastIndex = event.index + event.length;
        }

        if (lastIndex < text.length()) {
            segments.add(new StyledSegment(text.substring(lastIndex), styleStack.peek()));
        }

        return segments;
    }

    // --- Helper classes for Phase 3 ---
    private enum EventType {OPEN, CLOSE}

    public record StyledSegment(String text, Style style) {
    }

    /**
     * Represents a potential delimiter like *, **, or __ found in the text.
     * It holds all the context needed to decide if it's part of a valid style pair.
     *
     * @param type '*' or '_' or '~' or '`'
     */
    private record Delimiter(int index, int length, char type, boolean canOpen, boolean canClose) {
    }

    /**
     * Represents a fully resolved, matched pair of delimiters defining a styled region.
     * This is the output of the processing phase.
     */
    private static class MatchedPair {
        final Style style;
        final int openIndex, openLength;
        final int closeIndex, closeLength;

        MatchedPair(Style style, Delimiter open, Delimiter close) {
            this.style = style;
            this.openIndex = open.index;
            this.openLength = open.length;
            this.closeIndex = close.index;
            this.closeLength = close.length;
        }
    }

    private record Event(int index, EventType type, Style style, int length) implements Comparable<Event> {

        @Override
        public int compareTo(Event o) {
            return Integer.compare(this.index, o.index);
        }
    }
}