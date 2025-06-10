package org.skyblockagent.network;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MarkdownParser {
    public static final MarkdownParser INSTANCE = new MarkdownParser();
    private final StringBuilder markdownBuffer = new StringBuilder();
    private final List<MutableText> completedLines = new ArrayList<>();
    private final Stack<Formatting> activeStyles = new Stack<>();
    private MutableText currentLine = Text.empty();

    /**
     * Appends a chunk of Markdown text from the stream.
     * After appending, it attempts to parse the accumulated buffer.
     *
     * @param chunk The incoming piece of Markdown text.
     */
    public void processChunk(String chunk) {
        markdownBuffer.append(chunk);
        parseBuffer();
    }

    /**
     * Parses the accumulated markdownBuffer.
     * This is the core logic. It iterates through the buffer, identifies tokens,
     * and builds MutableText objects.
     */
    private void parseBuffer() {
        while (!markdownBuffer.isEmpty()) {
            String currentBufferState = markdownBuffer.toString(); // For startsWith checks

            // 1. Check for Newline
            if (currentBufferState.startsWith("\n")) {
                finalizeLine();
                markdownBuffer.delete(0, 1); // Consume "\n"
                continue;
            }
            // 2. Check for Bold (** or __)
            else if (currentBufferState.startsWith("**") || currentBufferState.startsWith("__")) {
                toggleStyle(Formatting.BOLD);
                markdownBuffer.delete(0, 2); // Consume "**"
                continue;
            }
            // 3. Check for Italic (* or _)
            //    Must ensure this doesn't misinterpret part of a bold marker if it's malformed
            //    (e.g., `*__italic*` - this logic handles it fine due to order)
            else if (currentBufferState.startsWith("*")) {
                toggleStyle(Formatting.ITALIC);
                markdownBuffer.delete(0, 1); // Consume "*"
                continue;
            } else if (currentBufferState.startsWith("_")) {
                toggleStyle(Formatting.ITALIC);
                markdownBuffer.delete(0, 1); // Consume "_"
                continue;
            }
            // 4. Plain Text Segment
            else {
                // Find the start of the next token
                int nextTokenIndex = markdownBuffer.length(); // Assume rest is plain if no token found

                int nlIdx = currentBufferState.indexOf("\n");
                if (nlIdx != -1 && nlIdx < nextTokenIndex) nextTokenIndex = nlIdx;

                int boldAstIdx = currentBufferState.indexOf("**");
                if (boldAstIdx != -1 && boldAstIdx < nextTokenIndex) nextTokenIndex = boldAstIdx;

                int boldUndIdx = currentBufferState.indexOf("__");
                if (boldUndIdx != -1 && boldUndIdx < nextTokenIndex) nextTokenIndex = boldUndIdx;

                int italicAstIdx = currentBufferState.indexOf("*");
                if (italicAstIdx != -1 && italicAstIdx < nextTokenIndex) nextTokenIndex = italicAstIdx;

                int italicUndIdx = currentBufferState.indexOf("_");
                if (italicUndIdx != -1 && italicUndIdx < nextTokenIndex) nextTokenIndex = italicUndIdx;

                // If nextTokenIndex is 0, it means we're at a token start that wasn't caught above.
                // This shouldn't happen if all token checks are comprehensive.
                // This case implies the buffer starts with a character that *is* a token but
                // our startsWith checks missed it, or it's a character like ` ` that is not a token.
                // If nextTokenIndex is 0, and we are in this 'else' block, it means the character
                // at the start of the buffer is NOT part of any defined token start sequence.
                // So, it must be the start of a plain text segment.
                // We need to ensure nextTokenIndex > 0 if we are to extract plain text up to it.
                // If nextTokenIndex is 0, it implies the current char is the start of a token we should have processed.
                // This path is for when the buffer *doesn't* start with a token.

                if (nextTokenIndex == 0) {
                    // This case should ideally not be hit if the `startsWith` checks cover all tokens.
                    // If it is hit, it means the character at markdownBuffer[0] is not a token starter.
                    // So, we treat it as plain text. We need to find the *next actual token*
                    // or consume one character as plain text if it's not a token.
                    // The logic below handles finding the segment of plain text.
                    // If nextTokenIndex is still 0 here, it implies the plain text segment is of length 1.
                    // Example: Buffer is "abc**". First pass, "a" is plain. nextTokenIndex could be 1 (for "b").
                    // No, nextTokenIndex means the START of the next token.
                    // If buffer is "a**", nextTokenIndex will be 1 (start of **). Plain text is "a".
                }


                String plainText;
                if (nextTokenIndex > 0) { // Found a token later in the string
                    plainText = markdownBuffer.substring(0, nextTokenIndex);
                } else { // No tokens found, or token is at start (which shouldn't happen here)
                    // If nextTokenIndex is length, means rest is plain text
                    // If nextTokenIndex is -1 (from indexOf not finding anything), rest is plain.
                    // The initial value of nextTokenIndex is markdownBuffer.length(), so this covers "rest is plain".
                    plainText = markdownBuffer.toString();
                }

                if (!plainText.isEmpty()) {
                    appendTextSegment(plainText);
                    markdownBuffer.delete(0, plainText.length());
                } else if (!markdownBuffer.isEmpty()) {
                    // Buffer has content, but we couldn't extract plain text and didn't match a token.
                    // This could happen if a chunk ends mid-token, e.g., buffer is just "*".
                    // In this case, we need to wait for more data.
                    break; // Exit parseBuffer, wait for next chunk.
                }
            }
        }
    }

    /**
     * Appends a segment of plain text to the currentLine, applying active styles.
     *
     * @param text The plain text segment.
     */
    private void appendTextSegment(String text) {
        if (text.isEmpty()) {
            return;
        }

        Style style = Style.EMPTY;
        for (Formatting formatting : activeStyles) {
            style = style.withFormatting(formatting);
        }
        currentLine = currentLine.copy().append(Text.literal(text).setStyle(style));
    }

    /**
     * Toggles a specific formatting style in the activeStyles stack.
     * If the style is already active (present in the stack), it's removed.
     * Otherwise, it's added (pushed onto the stack).
     * This simple toggle works for well-formed Markdown bold/italic.
     *
     * @param styleToToggle The Formatting to toggle.
     */
    private void toggleStyle(Formatting styleToToggle) {
        if (activeStyles.contains(styleToToggle)) {
            // Remove the most recently added instance of this style.
            // Stack.remove(Object) removes the first occurrence (bottom-most).
            // We need to remove the one that was pushed for this style.
            // A simple solution for non-overlapping styles of the same type:
            if (activeStyles.peek() == styleToToggle) {
                activeStyles.pop();
            } else {
                // If it's not on top, but present, means styles are crossed (e.g. **bold _italic** normal_)
                // For simplicity, remove the first occurrence found by iterating from top.
                // This is equivalent to Stack.removeElement(styleToToggle) behavior if we want to remove first from bottom
                // but we want to remove the most recent one.
                // A better way for simple toggle: if it's in there, remove it. Otherwise, add it.
                // This handles `**text _italic** text_` well.
                activeStyles.remove(styleToToggle); // Removes first occurrence from bottom.
                // For BOLD/ITALIC only, this usually works fine.
            }
        } else {
            activeStyles.push(styleToToggle);
        }
    }


    /**
     * Finalizes the currentLine, adds it to completedLines, and resets for a new line.
     * Active styles like bold/italic are NOT cleared here, as they can span newlines.
     */
    private void finalizeLine() {
        // Only add if the line has actual content (text or siblings with content)
        boolean hasContent = !currentLine.getString().isEmpty() ||
                (currentLine.getSiblings() != null && !currentLine.getSiblings().isEmpty());

        if (hasContent) {
            completedLines.add(currentLine);
        }
        currentLine = Text.empty();
    }

    /**
     * Retrieves all fully parsed lines and clears the internal list.
     *
     * @return A list of MutableText objects, each representing a displayable line.
     */
    public List<MutableText> getCompletedLinesAndClear() {
        List<MutableText> lines = new ArrayList<>(completedLines);
        completedLines.clear();
        return lines;
    }

    /**
     * Gets the current partially built line (text that hasn't been ended by a newline yet).
     * If the internal markdownBuffer still has unparsed content (e.g. waiting for more chunks
     * to complete a token), this method will append that raw buffer content to the stylistically
     * processed currentLine for preview purposes.
     *
     * @return The current, potentially incomplete, line with styles applied to processed parts.
     */
    public MutableText getCurrentLineText() {
        MutableText displayLine = currentLine.copy();

        // If there's remaining unparsed buffer, append it with current active styles for preview
        if (!markdownBuffer.isEmpty()) {
            Style previewStyle = Style.EMPTY;
            for (Formatting formatting : activeStyles) {
                previewStyle = previewStyle.withFormatting(formatting);
            }
            displayLine.append(Text.literal(markdownBuffer.toString()).setStyle(previewStyle));
        }
        return displayLine;
    }

    /**
     * Resets the parser state for a new conversation or message.
     */
    public void reset() {
        markdownBuffer.setLength(0);
        currentLine = Text.empty();
        completedLines.clear();
        activeStyles.clear();
    }
}
