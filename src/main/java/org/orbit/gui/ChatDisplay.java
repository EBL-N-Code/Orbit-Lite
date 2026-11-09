package org.apex.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import org.apex.Constants;
import org.apex.chat.Chat;
import org.apex.chat.ChatManager;
import org.apex.chat.message.ChatMessage;
import org.apex.chat.message.Toolbar;
import org.apex.gui.components.Scrollbar;
import org.apex.gui.components.Widget;
import org.apex.render.Renderer;

import java.util.List;

public class ChatDisplay extends Widget {
    public static final int OFFSET_BETWEEN_MESSAGES = 5;
    private static final int INITIAL_OFFSET = 15;
    private final Scrollbar scrollbar = new Scrollbar(1f, 0f);
    private Chat currentChat;
    private ChatMessage hoveredMsg;
    private int fullWidth, fullHeight;
    private int yOffset;

    @Override
    protected void init() {
        ChatScreen chatScreen = ChatScreen.getInstance();
        fullWidth = chatScreen.getChatWidth();
        fullHeight = chatScreen.getChatHeight();

        currentChat = ChatManager.getInstance().getChat();
        yOffset = INITIAL_OFFSET;
        for (ChatMessage message : currentChat.getMessages()) {
            message.init(x, y + yOffset, width);
            yOffset += message.getHeight() + OFFSET_BETWEEN_MESSAGES;
        }
        scrollbar.init(y, height, chatScreen.getRight(), yOffset);
    }

    /**
     * Recalculates the total vertical offset based on the heights of all messages in the current chat.
     * The offset includes the initial offset plus the sum of all message heights and spacing between them.
     */
    public void calculateOffset() {
        yOffset = INITIAL_OFFSET + currentChat.getMessages().stream().mapToInt(c -> c.getHeight() + OFFSET_BETWEEN_MESSAGES).sum();
    }

    /**
     * Handles the addition of a **NEW** chat message to the display.
     * initializes the message position, and updates the scrollbar.
     *
     * @param chatMessage the new message to add to the chat display
     */
    public void onMessage(ChatMessage chatMessage) {
        chatMessage.init(x, y + yOffset, width);
        yOffset += chatMessage.getHeight() + OFFSET_BETWEEN_MESSAGES;

        scrollbar.reset();
        scrollbar.updateOffset(yOffset, height);
    }

    /**
     * Handles updates to an existing chat message.
     * Recalculates offsets and updates the scrollbar if the updated chat matches the current chat.
     *
     * @param chat the chat that was updated
     */
    public void onMessageUpdate(Chat chat) {
        if (!chat.getUuid().equals(currentChat.getUuid())) return;
        calculateOffset();
        scrollbar.updateOffset(yOffset, height);
    }

    /**
     * Updates the display height and reinitializes the scrollbar with the new height.
     * Used by InputBar
     *
     * @param height the new height for the chat display
     */
    public void updateHeight(int height) {
        this.height = height;
        scrollbar.init(y, height, ChatScreen.getInstance().getRight(), yOffset);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        List<ChatMessage> chatMessages = ChatManager.getInstance().getChat().getMessages();
        if (chatMessages.isEmpty()) {
            renderWelcomeText(context);
            return;
        }

        float scrollOffset = scrollbar.getScrollOffset();
        int size = chatMessages.size();
        ChatMessage lastMsg = null;
        hoveredMsg = null;

        context.enableScissor(x-3, y, x + width, y + height + 12);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -scrollOffset);

        for (int i = size - 1; i >= 0; i--) {
            ChatMessage chatMessage = chatMessages.get(i);

            int messageTopY = chatMessage.getY();
            int messageBottomY = messageTopY + chatMessage.getHeight();

            if (messageBottomY <= y + scrollOffset) break;
            if (messageTopY < y + scrollOffset + height) {
                if (chatMessage.isHovered(mouseX, mouseY + scrollOffset)) hoveredMsg = chatMessage;
                if (i == size - 1) lastMsg = chatMessage;
                chatMessage.render(context);
            }
        }

        if (lastMsg != null) {
            Toolbar toolbar = lastMsg.getToolbar();
            if (toolbar != null) {
                toolbar.render(context, mouseX, mouseY, scrollOffset, true);
            }
        }

        if (hoveredMsg != null && (lastMsg == null || !lastMsg.equals(hoveredMsg))) {
            Toolbar toolbar = hoveredMsg.getToolbar();
            if (toolbar != null) {
                toolbar.render(context, mouseX, mouseY, scrollOffset, false);
            }
        }

        context.getMatrices().popMatrix();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    public void renderWelcomeText(DrawContext context) {
        String welcomeText = Constants.WELCOME + ", " + MinecraftClient.getInstance().getGameProfile().name();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + ((float) width / 2), y + ((float) height / 3));
        context.getMatrices().scale(1.5f, 1.5f);
        Renderer.drawCenteredText(context, welcomeText, 0, 0);
        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.y() >= y + height) return false;
        if (hoveredMsg != null) {
            Toolbar toolbar = hoveredMsg.getToolbar();
            if (toolbar != null && toolbar.mouseClicked()) return true;
        }
        return scrollbar.mouseClicked(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollbar.mouseScrolled(verticalAmount, yOffset, height);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return scrollbar.mouseDragged(deltaY, yOffset, height);
    }

    @Override
    public boolean mouseReleased(Click click) {
        return scrollbar.mouseReleased();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (mouseY >= y + height && ChatScreen.getInstance().getInputBar().isMouseOver(mouseX, mouseY)) return false;
        return mouseX >= x - ((double) fullWidth / 2) && mouseY >= y && mouseX < x + width + ((double) fullWidth / 2) && mouseY < y + fullHeight;
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }
}
