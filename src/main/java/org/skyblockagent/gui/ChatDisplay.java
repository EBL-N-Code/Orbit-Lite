package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class ChatDisplay extends Widget {
    private final Scrollbar scrollbar = new Scrollbar();
    private int chatX, chatWidth, chatHeight;
    private int maxUserMsgWidth;
    private float scrollOffset = 0f;
    private double scrollPercentage = 1f;
    private int yOffset;

    @Override
    protected void init() {
        this.yOffset = 10;
        this.maxUserMsgWidth = (chatWidth * 2) / 3;

        for (ChatMessage chatMessage : ChatManager.INSTANCE.getSession().getChatMessages()) {
            chatMessage.init(chatX, y + yOffset, chatWidth, maxUserMsgWidth);
            addSpace(chatMessage.getHeight() + 5);
        }
        updateScrollBar();
    }

    protected void init(int x, int y, int width, int height, int chatX, int chatWidth) {
        this.chatX = chatX;
        this.chatWidth = chatWidth;
        this.chatHeight = height;
        init(x, y, width, height);
    }

    public void onMessage(ChatMessage chatMessage) {
        chatMessage.init(chatX, y + yOffset, chatWidth, maxUserMsgWidth);

        scrollPercentage = 1f;
        addSpace(chatMessage.getHeight() + 5);
        updateScrollBar();
    }

    public void addSpace(int space) {
        yOffset += space;
    }

    public void updateHeight(int height) {
        this.chatHeight = height;
        updateScrollBar();
    }

    public void updateScrollBar() {
        int minY = y + 5;
        int maxY = y + chatHeight - 5;

        scrollbar.init(minY, maxY, ChatScreen.INSTANCE.getRight());

        boolean isScrollable = yOffset > chatHeight;
        scrollbar.setVisibility(isScrollable);

        if (isScrollable) {
            float maxScroll = yOffset - chatHeight;
            scrollPercentage = MathHelper.clamp(scrollPercentage, 0.0f, 1.0f);
            scrollOffset = MathHelper.clamp((float) scrollPercentage * maxScroll, 0.0f, maxScroll);
            scrollbar.update(chatHeight, yOffset, scrollPercentage);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        RenderUtils.drawRect(context, x-1, y, width, height, new Vector4f(0f, 8f, 0f, 0f), 0xFF141519);

        List<ChatMessage> chatMessages = ChatManager.INSTANCE.getSession().getChatMessages();
        if (chatMessages.isEmpty()) {
            renderGreatingText(context, x + (width / 2), y + (height / 3));
            return;
        }

        context.enableScissor(chatX, y, chatX + chatWidth, y + chatHeight + 8);
        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        for (int i = chatMessages.size() - 1; i >= 0; i--) {
            ChatMessage chatMessage = chatMessages.get(i);

            int messageTopY = chatMessage.getY();
            int messageBottomY = messageTopY + chatMessage.getHeight();

            if (messageBottomY <= y + scrollOffset) {
                break;
            }

            if (messageTopY < y + scrollOffset + chatHeight) {
                chatMessage.render(context);
            }
        }

        context.getMatrices().pop();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    public void renderGreatingText(DrawContext context, int x, int y) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0f);
        context.getMatrices().scale(1.5f, 1.5f, 1f);
        context.drawCenteredTextWithShadow(textRenderer, "Good Evening, " + MinecraftClient.getInstance().getGameProfile().getName(), 0, 0, 0xFFFFFFFF);
        context.getMatrices().pop();
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
            float scrollDelta = (float) verticalAmount * 55f / (yOffset - chatHeight);
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
            scrollOffset = (float) (scrollPercentage * (yOffset - chatHeight));

            scrollbar.update(chatHeight, yOffset, scrollPercentage);
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
        return false;
    }

    public void resetPosition() {
        scrollOffset = 0f;
        scrollPercentage = 1f;
    }
}
