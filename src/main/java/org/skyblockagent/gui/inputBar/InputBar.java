package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Scrollbar;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.session.InputState;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class InputBar extends Widget {
    public static final int Y_OFFSET = 10;
    public static final int LINE_HEIGHT = 10;
    private static final int MAX_HEIGHT = LINE_HEIGHT * 5;
    private final SendButton sendButton = new SendButton();
    private final Scrollbar scrollbar = new Scrollbar(0f, 0f);
    protected TextEditor textEditor = new TextEditor();
    protected InputState inputState;
    protected long lastSwitchFocusTime;
    private boolean selectingText = false;
    private boolean dragging = false;
    private long lastClickTime = 0;
    private int clickCount = 0;
    private double mouseY;
    private boolean disabled;
    private int linesCount;

    @Override
    protected void init() {
        this.disabled = ChatManager.INSTANCE.hasActiveRequest();
        this.inputState = ChatManager.INSTANCE.getSession().getInputState();
        this.linesCount = -1;

        update();
    }

    protected void update() {
        lastSwitchFocusTime = Util.getMeasuringTimeMs();
        textEditor.wrapLines(width - 42);

        int linesSize = inputState.getLines().size();
        if (linesCount != linesSize) {
            linesCount = linesSize;
            ChatScreen chatScreen = ChatScreen.INSTANCE;

            int textHeight = Math.min(MAX_HEIGHT, getLinesHeight());

            height = 8 * 2 + textHeight;
            y = chatScreen.getBottom() - height - Y_OFFSET;

            sendButton.init(x + width - SendButton.SIZE - 5, y + height - SendButton.SIZE - 5);
            textEditor.init(x + 8, y + 8 + 1, width - 40, textHeight);
            scrollbar.init(textEditor.getY()-5, textEditor.getHeight()+10, x + width - 22, getLinesHeight());
            chatScreen.getChatDisplay().updateHeight(y - chatScreen.getTop());
        }

        updateScrollPosition(true);
    }

    /**
     * Updates the scroll position and scrollbar state based on the text editor's content.
     *
     * @param scrollToCursor If true, scrolls to ensure the cursor is visible.
     */
    private void updateScrollPosition(boolean scrollToCursor) {
        int linesHeight = getLinesHeight();
        boolean scrollable = linesHeight > MAX_HEIGHT;
        scrollbar.setVisibility(scrollable);

        if (scrollable) {
            scrollbar.calculateScrollOffset(linesHeight, MAX_HEIGHT);
            setScrollPercentage(scrollbar.getScrollPercentage());
            if (scrollToCursor) scrollToCursor();
            scrollbar.update(textEditor.getHeight(), linesHeight);
        }
    }

    /**
     * Adjusts the scroll offset to ensure the cursor is visible within the text editor.
     */
    private void scrollToCursor() {
        float maxScroll = getLinesHeight() - MAX_HEIGHT;
        int cursorLine = inputState.getCursorPosition().line;
        int cursorTopY = cursorLine * LINE_HEIGHT;
        int cursorBottomY = cursorTopY + LINE_HEIGHT;
        float scrollOffset = scrollbar.getScrollOffset();

        // If the cursor is above visible area
        if (cursorTopY <= scrollOffset) {
            scrollOffset = cursorTopY;
        } else if (cursorBottomY > scrollOffset + MAX_HEIGHT) {
            scrollOffset = (float) ((cursorBottomY - MAX_HEIGHT) / LINE_HEIGHT) * LINE_HEIGHT;
        }

        scrollOffset = MathHelper.clamp(scrollOffset, 0f, maxScroll);
        setScrollPos(scrollOffset, scrollOffset / maxScroll);
    }

    public void reset() {
        inputState = ChatManager.INSTANCE.getSession().getInputState();
        textEditor.reset();
        scrollbar.reset();
        linesCount = -1;

        update();
    }

    public boolean canSend() {
        return !textEditor.isEmpty() && !ChatManager.INSTANCE.hasActiveRequest();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 5f);

        RenderUtils.drawRect(context, x, y, width, height, 8, 0xFF1C1F24, 2f, 0xFF27292E);

        textEditor.renderWidget(context, mouseX, mouseY, deltaTicks);
        sendButton.render(context, mouseX, mouseY);
        scrollbar.render(context, mouseX, mouseY);

        context.getMatrices().pop();
    }

    /**
     * Automatically scrolls the text editor when dragging a text selection outside the visible area,
     * moving the cursor accordingly.
     */
    public void autoScroll() {
        if (dragging && textEditor.isFocused() && textEditor.hasSelection() && scrollbar.isVisible()) {
            int relativeY = (int) (mouseY - y - 8);
            int maxScroll = getLinesHeight() - MAX_HEIGHT;
            float scrollOffset = scrollbar.getScrollOffset();

            if (relativeY < 0) {
                scrollOffset = Math.max(scrollOffset - 5f, 0f);
                textEditor.moveCursorVertical(-1, true); // Move cursor up
            } else if (relativeY > MAX_HEIGHT) {
                scrollOffset = Math.min(scrollOffset + 5f, maxScroll);
                textEditor.moveCursorVertical(1, true); // Move cursor down
            }

            setScrollPos(scrollOffset, scrollOffset / maxScroll);
            scrollbar.update(textEditor.getHeight(), getLinesHeight());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.lastSwitchFocusTime = Util.getMeasuringTimeMs();

        if (!disabled) {
            if (scrollbar.mouseClicked(mouseX, mouseY)) return true;
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
        if (textEditor.isFocused() && scrollbar.isVisible()) {
            scrollbar.calculateScrollPercentage(verticalAmount, getLinesHeight(), MAX_HEIGHT);
            setScrollPercentage(scrollbar.getScrollPercentage());
            updateScrollPosition(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollbar.mouseDragged(deltaY, getLinesHeight(), textEditor.getHeight())) {
            setScrollPercentage(scrollbar.getScrollPercentage());
            return true;
        } else if (textEditor.isFocused() && selectingText) {
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
        scrollbar.mouseReleased();
        dragging = false;
        selectingText = false;
        return super.mouseReleased(mouseX, mouseY, button);
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
        int pixelX = MathHelper.floor(mouseX) - x - 8;
        int pixelY = MathHelper.floor(mouseY) - y - 8 + (int) scrollbar.getScrollOffset() + 2;

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
        if (ChatScreen.INSTANCE == null) return;
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

    public void setScrollPercentage(double scrollPercentage) {
        inputState.setScrollPercentage(scrollPercentage);
    }

    public void setScrollPos(float scrollOffset, double scrollPercentage) {
        scrollbar.setScrollOffset(scrollOffset);
        scrollbar.setScrollPercentage(scrollPercentage);
        inputState.setScrollPercentage(scrollPercentage);
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }
}