package org.apex.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apex.Apex;
import org.apex.chat.ChatManager;
import org.apex.config.ModState;
import org.apex.gui.ChatScreen;
import org.apex.gui.components.Button;
import org.apex.gui.components.Scrollbar;
import org.apex.gui.components.Widget;
import org.apex.network.BackendService;
import org.apex.network.HttpUtils;
import org.apex.render.Renderer;

import java.util.List;

public class InputBar extends Widget {
    private static final int MAX_LENGTH = 2000;
    public static final int Y_OFFSET = 10;
    public static final int LINE_HEIGHT = 10;
    private static final int MAX_HEIGHT = LINE_HEIGHT * 5;
    private final SendButton sendButton = new SendButton();
    private final Scrollbar scrollbar = new Scrollbar();
    protected TextEditor textEditor = new TextEditor(this);
    protected InputState inputState;
    protected long lastSwitchFocusTime;
    private boolean selectingText = false;
    private boolean dragging = false;
    private int clickCount = 0;
    private int length = 0;
    private double mouseYPos;
    private int linesCount;

    @Override
    protected void init() {
        ChatManager chatManager = ChatManager.getInstance();
        this.inputState = chatManager.getChat().getInputState();
        this.scrollbar.setScrollPercentage(inputState.getScrollPercentage());
        this.linesCount = -1;

        update();
    }

    protected void update() {
        lastSwitchFocusTime = Util.getMeasuringTimeMs();
        textEditor.wrapLines(width - 42);
        checkLength();

        int linesSize = inputState.getLines().size();
        if (linesCount != linesSize) {
            linesCount = linesSize;
            ChatScreen chatScreen = ChatScreen.getInstance();

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

    private void checkLength() {
        List<String> lines = inputState.getLines();
        length = 0;

        for (String line : lines) {
            length += line.length();
        }
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
        int cursorLine = inputState.getCursor().line;
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

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        boolean cannotSend = isOverLimit() || ModState.isDisabled();
        int borderColor = cannotSend ? 0xFFFF5555 : 0xFF27292E;
        Renderer.drawRect(context, x, y, width, height, 8, 0xFF1C1F24, borderColor, 1f);

        if (isHovered() && !cannotSend) context.setCursor(StandardCursors.IBEAM);

        textEditor.renderWidget(context, mouseX, mouseY, deltaTicks);
        sendButton.render(context, mouseX, mouseY);
        scrollbar.render(context, mouseX, mouseY);
    }

    /**
     * Automatically scrolls the text editor when dragging a text selection outside the visible area,
     * moving the cursor accordingly.
     */
    public void autoScroll() {
        if (dragging && textEditor.isFocused() && textEditor.hasSelection() && scrollbar.isVisible()) {
            int relativeY = (int) (mouseYPos - y - 8);
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
    public boolean mouseClicked(Click click, boolean doubled) {
        this.lastSwitchFocusTime = Util.getMeasuringTimeMs();

        if (scrollbar.mouseClicked(click)) return true;
        if (sendButton.isHovered()) {
            if (canSend()) {
                ChatManager.getInstance().getChat().send();
                reset();
            } else if (BackendService.hasActiveCall()) {
                HttpUtils.interceptStreaming();
            }
            textEditor.setFocused(false);
            return true;
        }

        clickCount = doubled ? clickCount + 1 : 1;
        textEditor.setFocused(true);
        textEditor.onClick(mapMouseToCursor(click), clickCount, click.hasShift());
        selectingText = true;
        return true;
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
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (scrollbar.mouseDragged(deltaY, getLinesHeight(), textEditor.getHeight())) {
            setScrollPercentage(scrollbar.getScrollPercentage());
            return true;
        } else if (textEditor.isFocused() && selectingText) {
            CursorPosition draggedPosition = mapMouseToCursor(click);
            inputState.setSelectionEnd(draggedPosition);
            inputState.setCursor(draggedPosition);

            this.dragging = true;
            this.mouseYPos = click.y();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        scrollbar.mouseReleased();
        dragging = false;
        selectingText = false;
        return false;
    }

    public void reset() {
        textEditor.reset();
        scrollbar.reset();
        linesCount = -1;

        update();
    }

    public boolean canSend() {
        return !textEditor.isEmpty() && !BackendService.hasActiveCall() && !ModState.isDisabled() && !isOverLimit();
    }

    public boolean isOverLimit() {
        return length > MAX_LENGTH;
    }

    /**
     * Converts mouse coordinates to a cursor position within the text editor.
     *
     * @param click Mouse click.
     * @return The corresponding cursor position (line and column).
     */
    private CursorPosition mapMouseToCursor(Click click) {
        List<String> lines = inputState.getLines();
        int pixelX = MathHelper.floor(click.x()) - x - 8;
        int pixelY = MathHelper.floor(click.y()) - y - 8 + (int) scrollbar.getScrollOffset() + 2;

        int line = MathHelper.clamp(pixelY / LINE_HEIGHT, 0, lines.size() - 1);
        String lineText = lines.get(line);
        int column = textEditor.findColumnForPixelX(lineText, pixelX);
        column = MathHelper.clamp(column, 0, lineText.length());

        return new CursorPosition(line, column);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (textEditor.isFocused()) {
            return switch (input.getKeycode()) {
                case InputUtil.GLFW_KEY_A, InputUtil.GLFW_KEY_Q -> {
                    if (input.hasCtrl()) {
                        textEditor.setCursor(new CursorPosition(inputState.getLines().size() - 1, inputState.getLines().getLast().length()), false);
                        inputState.setSelectionEnd(new CursorPosition());
                        yield true;
                    }
                    yield false;
                }
                case InputUtil.GLFW_KEY_Z, InputUtil.GLFW_KEY_W -> {
                    if (input.hasCtrl()) {
                        textEditor.undo();
                        yield true;
                    }
                    yield false;
                }
                case InputUtil.GLFW_KEY_Y -> {
                    if (input.hasCtrl()) {
                        textEditor.redo();
                        yield true;
                    }
                    yield false;
                }
                case InputUtil.GLFW_KEY_DELETE, InputUtil.GLFW_KEY_BACKSPACE -> {
                    int direction = input.getKeycode() == InputUtil.GLFW_KEY_DELETE ? 1 : -1;
                    textEditor.erase(direction, input.hasCtrl());
                    yield true;
                }
                case InputUtil.GLFW_KEY_RIGHT, InputUtil.GLFW_KEY_LEFT -> {
                    int direction = input.getKeycode() == InputUtil.GLFW_KEY_RIGHT ? 1 : -1;
                    if (input.hasCtrl()) {
                        textEditor.setCursor(textEditor.getWordSkipPosition(direction), input.hasShift());
                    } else {
                        textEditor.moveCursor(direction, input.hasShift());
                    }
                    yield true;
                }
                case InputUtil.GLFW_KEY_HOME, InputUtil.GLFW_KEY_END -> {
                    boolean start = input.getKeycode() == InputUtil.GLFW_KEY_HOME;
                    textEditor.setCursorInLine(input.hasShift(), start);
                    yield true;
                }
                case InputUtil.GLFW_KEY_UP, InputUtil.GLFW_KEY_DOWN -> {
                    int direction = input.getKeycode() == InputUtil.GLFW_KEY_UP ? -1 : 1;
                    textEditor.moveCursorVertical(direction, input.hasShift());
                    yield true;
                }
                case InputUtil.GLFW_KEY_ENTER, InputUtil.GLFW_KEY_KP_ENTER -> {
                    if (input.hasShift()) {
                        textEditor.insertNewLine();
                        yield true;
                    } else {
                        if (canSend()) {
                            ChatManager.getInstance().getChat().send();
                            reset();
                            yield true;
                        }
                        yield false;
                    }
                }
                default -> {
                    if (input.isCopy()) {
                        MinecraftClient.getInstance().keyboard.setClipboard(textEditor.getSelection());
                        yield true;
                    } else if (input.isPaste()) {
                        textEditor.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
                        yield true;
                    } else if (input.isCut()) {
                        MinecraftClient.getInstance().keyboard.setClipboard(textEditor.getSelection());
                        textEditor.cutSelection();
                        yield true;
                    }
                    yield false;
                }
            };
        }
        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (textEditor.isFocused() && input.isValidChar()) {
            textEditor.insertText(input.asString());
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        textEditor.setFocused(focused);
    }

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

    public static class SendButton extends Button {
        private static final Identifier ARROW_TEXTURE = Identifier.of(Apex.NAMESPACE, "textures/ui/arrow.png");
        protected static final int SIZE = 16;

        public SendButton() {
            super(SIZE, SIZE);
        }

        @Override
        public void renderButton(DrawContext context) {
            Renderer.drawCircle(context, x, y, width, height, 0xFFFFFFFF);

            if (BackendService.hasActiveCall()) {
                Renderer.drawRect(context, x + 5, y + 5, 6, 6, 0, 0xFF000000);
            } else {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, x + 4, y + 4, 0, 0, 8, 8, 8, 8);
                if (!ChatScreen.getInstance().getInputBar().canSend()) {
                    if (isHovered) context.setCursor(StandardCursors.ARROW);
                    Renderer.drawCircle(context, x, y, width, height, 0x80000000);
                    return;
                }
            }

            if (isHovered) Renderer.drawCircle(context, x, y, width, height, 0x60000000);
        }
    }
}