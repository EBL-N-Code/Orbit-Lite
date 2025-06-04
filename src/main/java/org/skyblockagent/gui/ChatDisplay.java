package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.components.Scrollbar;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class ChatDisplay extends Widget {
    private static final String WELCOME_TEXT = "Welcome back, " + MinecraftClient.getInstance().getGameProfile().getName();
    private final Scrollbar scrollbar = new Scrollbar(1f, 0f);
    private int fullWidth, fullHeight;
    private int maxUserMsgWidth;
    private int yOffset;

    @Override
    protected void init() {
        ChatScreen chatScreen = ChatScreen.INSTANCE;
        this.fullWidth = chatScreen.getChatWidth();
        this.fullHeight = chatScreen.getChatHeight();
        this.maxUserMsgWidth = (width * 2) / 3;
        this.yOffset = 10;

        for (ChatMessage chatMessage : ChatManager.INSTANCE.getSession().getChatMessages()) {
            chatMessage.init(x, y + yOffset, width, maxUserMsgWidth);
            yOffset += chatMessage.getHeight() + 5;
        }
        scrollbar.init(y, height, chatScreen.getRight(), yOffset);
    }

    public void onMessage(ChatMessage chatMessage) {
        chatMessage.init(x, y + yOffset, width, maxUserMsgWidth);

        scrollbar.reset();
        addSpace(chatMessage.getHeight() + 5);
    }

    public void addSpace(int space) {
        yOffset += space;
        scrollbar.updateOffset(yOffset, height);
    }

    public void updateHeight(int newHeight) {
        height = newHeight;
        scrollbar.init(y, height, ChatScreen.INSTANCE.getRight(), yOffset);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        List<ChatMessage> chatMessages = ChatManager.INSTANCE.getSession().getChatMessages();
        if (chatMessages.isEmpty()) {
            renderGreatingText(context);
            return;
        }

        float scrollOffset = scrollbar.getScrollOffset();
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

    public void renderGreatingText(DrawContext context) {
        context.getMatrices().push();
        context.getMatrices().translate(x + ((float) width / 2), y + ((float) height / 3), 0f);
        context.getMatrices().scale(1.5f, 1.5f, 1f);
        RenderUtils.drawCenteredText(context, WELCOME_TEXT, 0, 0);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return scrollbar.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollbar.mouseScrolled(verticalAmount, yOffset, height);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return scrollbar.mouseDragged(deltaY, yOffset, height);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return scrollbar.mouseReleased();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x - ((double) fullWidth / 2) && mouseY >= y && mouseX < x + width + ((double) fullWidth / 2) && mouseY < y + fullHeight;
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }
}
