package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.Scrollbar;
import org.skyblockagent.gui.Widget;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.InputState;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class InputBar extends Widget {
    private static final int BAR_FILL_COLOR = 0xFF1C1F24;
    private static final int BAR_OUTLINE_COLOR = 0xFF27292E;
    public static final int Y_OFFSET = 10;
    public static final int LINE_HEIGHT = 10;
    private static final int MAX_HEIGHT = LINE_HEIGHT * 5;
    private static final float SCROLL_SENSITIVITY = 55f;
    private static final float AUTO_SCROLL_SPEED = 1f;
    private static final int CORNER_RADIUS = 8;
    private final SendButton sendButton = new SendButton();
    private final Scrollbar scrollbar = new Scrollbar();
    protected TextEditor textEditor;
    protected InputState inputState;
    protected long lastSwitchFocusTime;
    private boolean selectingText = false;
    private boolean dragging = false;
    private long lastClickTime = 0;
    private int clickCount = 0;
    private double mouseY;
    private boolean isScrollable;
    private float scrollOffset = 0f;
    private boolean disabled;

    @Override
    protected void init() {
        this.disabled = ChatManager.INSTANCE.hasActiveRequest();
        this.inputState = ChatManager.INSTANCE.getSession().getInputState();
        this.textEditor = new TextEditor(this);

        update(true);
    }

    protected void update(boolean scrollToCursor) {
        ChatScreen chatScreen = ChatScreen.INSTANCE;
        lastSwitchFocusTime = Util.getMeasuringTimeMs();

        int textWidth = width - 40;
        textEditor.wrapLines(textWidth - 2);
        int textHeight = Math.min(MAX_HEIGHT, getLinesHeight());

        height = CORNER_RADIUS * 2 + textHeight;
        y = chatScreen.getBottom() - height - Y_OFFSET;

        sendButton.init(x + width - SendButton.SIZE - 5, y + height - SendButton.SIZE - 5);
        textEditor.init(x + CORNER_RADIUS, y + CORNER_RADIUS + 1, textWidth, textHeight);
        scrollbar.init(textEditor.getY(), textEditor.getY() + textEditor.getHeight(), x + width - 22);
        chatScreen.getChatDisplay().updateHeight(y - chatScreen.getTop());

        updateScrollPosition(inputState.getLines().size() > MAX_HEIGHT / LINE_HEIGHT, scrollToCursor);
    }

    /**
     * Updates the scroll position and scrollbar state based on the text editor's content.
     *
     * @param scrollable     True if the content exceeds the visible area, enabling scrolling.
     * @param scrollToCursor If true, scrolls to ensure the cursor is visible.
     */
    private void updateScrollPosition(boolean scrollable, boolean scrollToCursor) {
        if (isScrollable && !scrollable) scrollOffset = 0f;

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
        } else if (cursorBottomY > scrollOffset + MAX_HEIGHT) {
            scrollOffset = (float) ((cursorBottomY - MAX_HEIGHT + LINE_HEIGHT) / LINE_HEIGHT) * LINE_HEIGHT;
        }

        scrollOffset = MathHelper.clamp(scrollOffset, 0f, maxScroll);
        setScrollPercentage(scrollOffset / maxScroll);
    }

    public void reset() {
        inputState = ChatManager.INSTANCE.getSession().getInputState();
        textEditor.reset();
        scrollOffset = 0f;

        update(true);
    }

    public boolean canSend() {
        return !textEditor.isEmpty() && !ChatManager.INSTANCE.hasActiveRequest();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 5f);

        RenderUtils.drawRect(context, x, y, width, height, CORNER_RADIUS, BAR_FILL_COLOR, 2f, BAR_OUTLINE_COLOR);

        textEditor.render(context, scrollOffset);
        sendButton.render(context, mouseX, mouseY);
        scrollbar.render(context, mouseX, mouseY);

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.lastSwitchFocusTime = Util.getMeasuringTimeMs();

        if (!disabled) {
            if (scrollbar.isHovered(mouseX, mouseY)) {
                scrollbar.setDragging(true);
                return true;
            }

            if (sendButton.isHovered(mouseX, mouseY)) {
                if (canSend()) sendButton.onClick();
                textEditor.setFocused(false);
                return true;
            }

            long currentTime = System.currentTimeMillis();
            clickCount = currentTime - lastClickTime <= MAX_DOUBLE_CLICK_INTERVAL ? clickCount + 1 : 1;
            lastClickTime = currentTime;
            textEditor.setFocused(true);
            textEditor.onClick(mapMouseToCursor(mouseX, mouseY), clickCount);
            selectingText = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (textEditor.isFocused() && isScrollable) {
            float scrollDelta = (float) verticalAmount * SCROLL_SENSITIVITY / getMaxScroll();
            setScrollPercentage(MathHelper.clamp(getScrollPercentage() - scrollDelta, 0.0f, 1.0f));
            update(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollbar.isDragging()) {
            int minY = scrollbar.getMinY();
            int maxY = scrollbar.getMaxY();
            int barHeight = scrollbar.getHeight();

            double newBarY = MathHelper.clamp(scrollbar.getY() + deltaY, minY, maxY - barHeight);
            double scrollPercentage = (newBarY - minY) / ((maxY - minY) - barHeight);
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
        int relativeY = (int) (mouseY - y - CORNER_RADIUS);
        int maxScroll = getMaxScroll();

        if (relativeY < 0) {
            scrollOffset = Math.max(scrollOffset - AUTO_SCROLL_SPEED, 0f);
            textEditor.moveCursorVertical(-1, true); // Move cursor up
        } else if (relativeY > MAX_HEIGHT) {
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
        int pixelX = MathHelper.floor(mouseX) - x - CORNER_RADIUS;
        int pixelY = MathHelper.floor(mouseY) - y - CORNER_RADIUS + (int) scrollOffset + 2;

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
                    if (Screen.hasShiftDown()) {
                        textEditor.insertNewLine();
                        yield true;
                    } else {
                        if (canSend()) {
                            ChatManager.INSTANCE.getSession().send();
                            yield true;
                        }
                        yield false;
                    }
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
        if (ChatManager.INSTANCE.hasActiveRequest()) return;
        if (focused) disabled = false;
        super.setFocused(focused);
        textEditor.setFocused(focused);
    }

    public void setActivation(boolean focused) {
        if (!focused) disabled = true;

        if (!focused && ChatScreen.INSTANCE.getFocused() == this) {
            ChatScreen.INSTANCE.setFocused(null);
        } else if (focused) {
            ChatScreen.INSTANCE.setFocused(this);
        }
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
        return (inputState.getLines().size() * LINE_HEIGHT) - MAX_HEIGHT;
    }

    public void setScrollPercentage(double scrollPercentage) {
        inputState.setScrollPercentage(scrollPercentage);
    }

    public double getScrollPercentage() {
        return inputState.getScrollPercentage();
    }
}