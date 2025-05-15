package org.skyblockagent.gui.chatDisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.gui.inputBar.CursorPosition;
import org.skyblockagent.gui.inputBar.Scrollbar;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class ChatDisplay extends ClickableWidget {
    private int x, y;
    private int maxWidth;
    private int yOffset = 20;
    private float scrollOffset = 0f;
    private double scrollPercentage = 1f;
    private boolean isScrollable;
    private final Scrollbar scrollbar;

    public ChatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.x = x;
        this.y = y;
        this.maxWidth = (width * 2) / 3;
        this.scrollbar = new Scrollbar();

        for (ChatMessage chatMessage : ChatManager.INSTANCE.session.getChatMessages()) {
            initMessage(chatMessage);
        }
        init();
    }

    public void initMessage(ChatMessage chatMessage) {
        chatMessage.setDimentions(maxWidth);
        chatMessage.setPosition(x + width - chatMessage.getWidth(), y + yOffset);
        yOffset += chatMessage.getHeight() + 5;
    }

    public void init() {
        scrollbar.init(x, y, width, 10);
        updateScrollPosition(yOffset > height);
    }

    private void updateScrollPosition(boolean scrollable) {
        if (isScrollable && !scrollable) {
            scrollOffset = 0f;
            scrollPercentage = 1f;
        }

        isScrollable = scrollable;
        scrollbar.setVisibility(isScrollable);

        if (isScrollable) {
            float maxScroll = yOffset - height;
            scrollPercentage = MathHelper.clamp(scrollPercentage, 0.0f, 1.0f);
            scrollOffset = MathHelper.clamp((float) scrollPercentage * maxScroll, 0.0f, maxScroll);
            scrollbar.update(height, yOffset, scrollPercentage);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        List<ChatMessage> chatMessages = ChatManager.INSTANCE.session.getChatMessages();

        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.render(context);
        }

        context.getMatrices().pop();

        if (scrollbar.isVisible()) scrollbar.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isScrollable) {
            float scrollDelta = (float) verticalAmount * 55f / (height - yOffset);
            scrollPercentage = MathHelper.clamp(scrollPercentage - scrollDelta, 0.0f, 1.0f);
            init();
            return true;
        }
        return false;
    }

//    @Override
//    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
//        if (scrollbar.isDragging()) {
//            int scrollTrackStartY = textEditor.getY();
//            int scrollTrackEndY = textEditor.getY() + textEditor.getHeight() - scrollbar.getHeight();
//
//            double newBarY = MathHelper.clamp(scrollbar.getY() + deltaY, scrollTrackStartY, scrollTrackEndY);
//            double scrollPercentage = (newBarY - scrollTrackStartY) / (scrollTrackEndY - scrollTrackStartY);
//            scrollOffset = (float) (scrollPercentage * getMaxScroll());
//            setScrollPercentage(scrollPercentage);
//
//            scrollbar.update(textEditor.getHeight(), getLinesHeight(), scrollPercentage);
//            return true;
//        }
//
//        if (textEditor.isFocused() && selectingText) {
//            CursorPosition draggedPosition = mapMouseToCursor(mouseX, mouseY);
//            inputState.setSelectionEnd(draggedPosition);
//            inputState.setCursorPosition(draggedPosition);
//
//            this.dragging = true;
//            this.mouseY = mouseY;
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        dragging = false;
//        selectingText = false;
//        scrollbar.setDragging(false);
//        return super.mouseReleased(mouseX, mouseY, button);
//    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
