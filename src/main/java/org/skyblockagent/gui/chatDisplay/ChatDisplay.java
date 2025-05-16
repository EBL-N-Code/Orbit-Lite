package org.skyblockagent.gui.chatDisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.gui.ChatScreen;
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

    public void updateHeight(int height) {
        this.height = height;
        init();
    }

    public void init() {
        ChatScreen chatScreen = ChatScreen.INSTANCE;
        scrollbar.init(y + 10, y + height - 10, chatScreen.getX() + chatScreen.getWidth());
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

        context.enableScissor(x, y, x + width, y + height + 12);
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
        if (isScrollable) {
            float scrollDelta = (float) verticalAmount * 55f / (yOffset - height);
            scrollPercentage = MathHelper.clamp(scrollPercentage - scrollDelta, 0.0f, 1.0f);
            init();
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

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
