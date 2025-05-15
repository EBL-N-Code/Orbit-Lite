package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.InputState;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class InputBar extends ClickableWidget {
    private static final int BAR_FILL_COLOR = 0xFF1B1C1D;
    private static final int BAR_OUTLINE_COLOR = 0xFF424447;
    private static final float BAR_OUTLINE_THICKNESS = 0.5f;
    public static final int Y_OFFSET = 10;
    public static final int LINE_HEIGHT = 10;
    private static final long DOUBLE_CLICK_MS = 250;
    private static final float SCROLL_SENSITIVITY = 55f;
    private static final float AUTO_SCROLL_SPEED = 1f;
    public static final int BAR_ROUNDNESS = 12;
    private final int maxVisibleLines = 5;
    private final int maxVisibleLinesHeight = LINE_HEIGHT * maxVisibleLines;
    private final int cornerRadius;
    private final int x;
    private int y;
    private final int baseHeight;
    private final Scrollbar scrollbar;
    protected final TextEditor textEditor;
    private final SendButton sendButton;
    private final List<Button> controlButtons;
    protected long lastSwitchFocusTime = Util.getMeasuringTimeMs();
    private boolean selectingText = false;
    private boolean dragging = false;
    private long lastClickTime = 0;
    private int clickCount = 0;
    private double mouseY;
    private boolean isScrollable;
    private float scrollOffset = 0f;
    protected final InputState inputState;

    public InputBar(int x, int y, int width, int height, int cornerRadius, int baseHeight, List<Button> controlButtons) {
        super(x, y, width, height, Text.empty());
        this.x = x;
        this.y = y;
        this.cornerRadius = cornerRadius;
        this.baseHeight = baseHeight;

        this.controlButtons = controlButtons;
        this.inputState = ChatManager.INSTANCE.session.getInputState();
        this.textEditor = new TextEditor(this, this.width - cornerRadius * 2);
        this.sendButton = new SendButton(this);
        this.scrollbar = new Scrollbar(this);

        //preview or no maybe
        if (ChatManager.INSTANCE.session.getInputState().getLines().isEmpty()) {
            init();
        } else {
            refreshLayout(true);
        }
    }

    public void reset(boolean firstMessage) {
        textEditor.reset();
        scrollOffset = 0f;
        height = baseHeight + LINE_HEIGHT;

        if (firstMessage) {
            ChatScreen chatScreen = ChatScreen.INSTANCE;
            y = chatScreen.getY() + chatScreen.getHeight() - height - Y_OFFSET;
            setY(y);
        }

        init();
        refreshLayout(true);
    }

    private void init() {
        // TextEditor
        textEditor.init(x + cornerRadius, y + cornerRadius, width - cornerRadius * 2, Math.min(getLinesHeight(), maxVisibleLinesHeight));

        // Buttons
        int xOffset = 0;
        for (Button button : controlButtons) {
            button.init(x + cornerRadius + xOffset - 4, y + height - cornerRadius - button.getHeight() + 6);
            xOffset += button.getWidth() + 5;
        }

        // Send Button
        sendButton.init(x + width - (cornerRadius / 2) - SendButton.SIZE, y + height - cornerRadius - SendButton.SIZE + 6);

        // Scrollbar
        scrollbar.init(cornerRadius);
    }

    /**
     * Updates the input bar's layout and scroll state based on the text editor's content.
     *
     * @param scrollToCursor If true, scrolls to ensure the cursor is visible.
     */
    protected void refreshLayout(boolean scrollToCursor) {
        textEditor.wrapLines();

        int linesHeight = getLinesHeight();
        height = baseHeight + Math.min(maxVisibleLinesHeight, linesHeight);

        ChatScreen chatScreen = ChatScreen.INSTANCE;
        if (!ChatManager.INSTANCE.session.isPreview()) {
            int maxY = chatScreen.getY() + chatScreen.getHeight() - Y_OFFSET;
            y = maxY - height;
            setY(y);
        }

        init();
        updateScrollPosition(linesHeight > maxVisibleLinesHeight, scrollToCursor);

        lastSwitchFocusTime = Util.getMeasuringTimeMs();
    }

    /**
     * Updates the scroll position and scrollbar state based on the text editor's content.
     *
     * @param scrollable     True if the content exceeds the visible area, enabling scrolling.
     * @param scrollToCursor If true, scrolls to ensure the cursor is visible.
     */
    private void updateScrollPosition(boolean scrollable, boolean scrollToCursor) {
        if (isScrollable && !scrollable) {
            scrollOffset = 0f;
            setScrollPercentage(0f);
        }

        isScrollable = scrollable;
        scrollbar.setVisibility(isScrollable);

        if (isScrollable) {
            float maxScroll = getMaxScroll();
            double scrollPercentage = MathHelper.clamp(getScrollPercentage(), 0.0f, 1.0f);
            scrollOffset = MathHelper.clamp((float) scrollPercentage * maxScroll, 0.0f, maxScroll);
            setScrollPercentage(scrollPercentage);

            if (scrollToCursor) scrollToCursor();
            scrollbar.update(textEditor.getHeight(), getLinesHeight(), getScrollPercentage());
        }
    }

    /**
     * Adjusts the scroll offset to ensure the cursor is visible within the text editor.
     */
    private void scrollToCursor() {
        float maxScroll = getMaxScroll();
        int cursorLine = inputState.getCursorPosition().line;
        int cursorTopY = cursorLine * LINE_HEIGHT;
        int cursorBottomY = cursorTopY + LINE_HEIGHT;

        // If cursor is above visible area
        if (cursorTopY <= scrollOffset) {
            scrollOffset = cursorTopY;
        } else if (cursorBottomY > scrollOffset + maxVisibleLinesHeight) {
            scrollOffset = (float) ((cursorBottomY - maxVisibleLinesHeight + LINE_HEIGHT) / LINE_HEIGHT) * LINE_HEIGHT;
        }

        scrollOffset = MathHelper.clamp(scrollOffset, 0f, maxScroll);
        setScrollPercentage(scrollOffset / maxScroll);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 5f);

        RenderUtils.drawRoundedRect(context, x, y, width, height, cornerRadius, BAR_FILL_COLOR, BAR_OUTLINE_THICKNESS, BAR_OUTLINE_COLOR);

        textEditor.render(context, scrollOffset);
        controlButtons.forEach(b -> b.render(context, mouseX, mouseY));
        sendButton.render(context, mouseX, mouseY);
        if (scrollbar.isVisible()) scrollbar.render(context, mouseX, mouseY);

        context.getMatrices().pop();
    }

    /**
     * Updates the input bar's state each tick, handling auto-scrolling during text selection.
     */
    public void tick() {
        if (dragging && textEditor.isFocused() && textEditor.hasSelection()) {
            autoScrollWhileDragging();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.setFocused(true);
        this.lastSwitchFocusTime = Util.getMeasuringTimeMs();

        if (textEditor.isHovered(mouseX, mouseY)) {
            long currentTime = System.currentTimeMillis();
            clickCount = currentTime - lastClickTime <= DOUBLE_CLICK_MS ? clickCount + 1 : 1;
            lastClickTime = currentTime;
            textEditor.setFocused(true);
            textEditor.onClick(mapMouseToCursor(mouseX, mouseY), clickCount);
            selectingText = true;
            return;
        }

        if (scrollbar.isVisible() && scrollbar.isHovered(mouseX, mouseY)) {
            scrollbar.setDragging(true);
            return;
        }

        textEditor.setFocused(false);

        if (sendButton.isHovered(mouseX, mouseY)) {
            sendButton.onClick();
            return;
        }

        for (Button button : controlButtons) {
            if (button.isHovered(mouseX, mouseY)) {
                button.onClick();
                return;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (textEditor.isFocused() && isScrollable) {
            float scrollDelta = (float) verticalAmount * SCROLL_SENSITIVITY / getMaxScroll();
            setScrollPercentage(MathHelper.clamp(getScrollPercentage() - scrollDelta, 0.0f, 1.0f));
            refreshLayout(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollbar.isDragging()) {
            int scrollTrackStartY = textEditor.getY();
            int scrollTrackEndY = textEditor.getY() + textEditor.getHeight() - scrollbar.getHeight();

            double newBarY = MathHelper.clamp(scrollbar.getY() + deltaY, scrollTrackStartY, scrollTrackEndY);
            double scrollPercentage = (newBarY - scrollTrackStartY) / (scrollTrackEndY - scrollTrackStartY);
            scrollOffset = (float) (scrollPercentage * getMaxScroll());
            setScrollPercentage(scrollPercentage);

            scrollbar.update(textEditor.getHeight(), getLinesHeight(), scrollPercentage);
            return true;
        }

        if (textEditor.isFocused() && selectingText) {
            CursorPosition draggedPosition = mapMouseToCursor(mouseX, mouseY);
            inputState.setSelectionEnd(draggedPosition);
            inputState.setCursorPosition(draggedPosition);

            this.dragging = true;
            this.mouseY = mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        selectingText = false;
        scrollbar.setDragging(false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Automatically scrolls the text editor when dragging a text selection outside the visible area,
     * moving the cursor accordingly.
     */
    private void autoScrollWhileDragging() {
        int relativeY = (int) (mouseY - y - cornerRadius);
        int maxScroll = getMaxScroll();

        if (relativeY < 0) {
            scrollOffset = Math.max(scrollOffset - AUTO_SCROLL_SPEED, 0f);
            textEditor.moveCursorVertical(-1, true); // Move cursor up
        } else if (relativeY > maxVisibleLinesHeight) {
            scrollOffset = Math.min(scrollOffset + AUTO_SCROLL_SPEED, maxScroll);
            textEditor.moveCursorVertical(1, true); // Move cursor down
        }

        setScrollPercentage(maxScroll > 0f ? scrollOffset / maxScroll : 0f);
    }

    /**
     * Converts mouse coordinates to a cursor position within the text editor.
     *
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @return The corresponding cursor position (line and column).
     */
    private CursorPosition mapMouseToCursor(double mouseX, double mouseY) {
        List<String> lines = inputState.getLines();
        int pixelX = MathHelper.floor(mouseX) - x - cornerRadius;
        int pixelY = MathHelper.floor(mouseY) - y - cornerRadius + (int) scrollOffset + 2;

        int line = MathHelper.clamp(pixelY / LINE_HEIGHT, 0, lines.size() - 1);
        String lineText = lines.get(line);
        int column = textEditor.findColumnForPixelX(lineText, pixelX);
        column = MathHelper.clamp(column, 0, lineText.length());

        return new CursorPosition(line, column);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textEditor.isFocused()) {
            return switch (keyCode) {
                case 81 -> {
                    if (Screen.hasControlDown()) {
                        textEditor.setCursor(new CursorPosition(inputState.getLines().size() - 1, inputState.getLines().getLast().length()), false);
                        inputState.setSelectionEnd(new CursorPosition(0, 0));
                        yield true;
                    }
                    yield false;
                }
                case 87 -> {
                    if (Screen.hasControlDown()) {
                        textEditor.undo();
                        yield true;
                    }
                    yield false;
                }
                case GLFW.GLFW_KEY_Y -> { // Ctrl+Y (Redo)
                    if (Screen.hasControlDown()) {
                        textEditor.redo();
                        yield true;
                    }
                    yield false;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> { // Backspace
                    textEditor.erase(-1, Screen.hasControlDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_DELETE -> { // Delete
                    textEditor.erase(1, Screen.hasControlDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_RIGHT -> { // Right Arrow
                    if (Screen.hasControlDown()) {
                        textEditor.setCursor(textEditor.getWordSkipPosition(1), Screen.hasShiftDown());
                    } else {
                        textEditor.moveCursor(1, Screen.hasShiftDown());
                    }
                    yield true;
                }
                case GLFW.GLFW_KEY_LEFT -> { // Left Arrow
                    if (Screen.hasControlDown()) {
                        textEditor.setCursor(textEditor.getWordSkipPosition(-1), Screen.hasShiftDown());
                    } else {
                        textEditor.moveCursor(-1, Screen.hasShiftDown());
                    }
                    yield true;
                }
                case GLFW.GLFW_KEY_HOME -> { // Home
                    textEditor.setCursorToLineStart(Screen.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_END -> { // End
                    textEditor.setCursorToLineEnd(Screen.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_DOWN -> { // Down Arrow
                    textEditor.moveCursorVertical(1, Screen.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> { // Up Arrow
                    textEditor.moveCursorVertical(-1, Screen.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_ENTER -> { // Enter
                    textEditor.insertNewLine();
                    yield true;
                }
                default -> {
                    if (Screen.isCopy(keyCode)) {
                        MinecraftClient.getInstance().keyboard.setClipboard(textEditor.getSelection());
                        yield true;
                    } else if (Screen.isPaste(keyCode)) {
                        textEditor.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
                        yield true;
                    } else if (Screen.isCut(keyCode)) {
                        MinecraftClient.getInstance().keyboard.setClipboard(textEditor.getSelection());
                        textEditor.insertText("");
                        yield true;
                    }
                    yield false;
                }
            };
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textEditor.isFocused() && StringHelper.isValidChar(chr)) {
            textEditor.insertText(Character.toString(chr));
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        textEditor.setFocused(focused);
    }

    /**
     * Calculates the total height of the text editor's lines based on the line count and line height.
     *
     * @return The total height in pixels.
     */
    public int getLinesHeight() {
        return inputState.getLines().size() * LINE_HEIGHT;
    }

    /**
     * Calculates the maximum scrollable distance based on the text editor's line count.
     *
     * @return The maximum scroll distance in pixels.
     */
    private int getMaxScroll() {
        return (inputState.getLines().size() - maxVisibleLines) * LINE_HEIGHT;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public void setScrollPercentage(double scrollPercentage) {
        inputState.setScrollPercentage(scrollPercentage);
    }

    public double getScrollPercentage() {
        return inputState.getScrollPercentage();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }
}