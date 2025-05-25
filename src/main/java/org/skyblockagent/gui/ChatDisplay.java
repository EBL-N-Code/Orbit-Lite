package org.skyblockagent.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.ChatMessage;

import java.util.List;

public class ChatDisplay extends Widget {
    private final Scrollbar scrollbar = new Scrollbar();
    private float scrollOffset = 0f;
    private double scrollPercentage = 1f;
    private int yOffset;
    private int maxWidth;

    @Override
    protected void init() {
        this.yOffset = 10;
        this.maxWidth = (width * 2) / 3;

        for (ChatMessage chatMessage : ChatManager.INSTANCE.getSession().getChatMessages()) {
            chatMessage.init(x, y + yOffset, width, maxWidth);
            addSpace(chatMessage.getHeight() + 5);
        }
        updateScrollBar();
    }

    public void onMessage(ChatMessage chatMessage) {
        chatMessage.init(x, y + yOffset, width, maxWidth);

        scrollPercentage = 1f;
        addSpace(chatMessage.getHeight() + 5);
        updateScrollBar();
    }

    public void addSpace(int space) {
        yOffset += space;
    }

    public void updateHeight(int height) {
        this.height = height;
        updateScrollBar();
    }

    public void updateScrollBar() {
        scrollbar.init(y + 5, y + height - 5, ChatScreen.INSTANCE.getRight());

        boolean isScrollable = yOffset > height;
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
        List<ChatMessage> chatMessages = ChatManager.INSTANCE.getSession().getChatMessages();

        context.enableScissor(x, y, x + width, y + height + 8);
        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        for (int i = chatMessages.size() - 1; i >= 0; i--) {
            ChatMessage chatMessage = chatMessages.get(i);

            int messageTopY = chatMessage.getY();
            int messageBottomY = messageTopY + chatMessage.getHeight();

            if (messageBottomY <= y + scrollOffset) {
                break;
            }

            if (messageTopY < y + scrollOffset + height) {
                chatMessage.render(context);
            }
        }

        context.getMatrices().pop();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollbar.isHovered(mouseX, mouseY)) {
            scrollbar.setDragging(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (scrollbar.isVisible()) {
            float scrollDelta = (float) verticalAmount * 55f / (yOffset - height);
            scrollPercentage = MathHelper.clamp(scrollPercentage - scrollDelta, 0.0f, 1.0f);
            updateScrollBar();
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
            scrollPercentage = (newBarY - minY) / ((maxY - minY) - barHeight);
            scrollOffset = (float) (scrollPercentage * (yOffset - height));

            scrollbar.update(height, yOffset, scrollPercentage);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollbar.isDragging()) {
            scrollbar.setDragging(false);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
