package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.ColorUtils;

public class Sidebar extends Widget {
    private static boolean isExpanded = false;
    private final ExpandButton expandButton = new ExpandButton();
    private final Button chatButton = new Button("+ New Chat");
    private final ChatList chatList = new ChatList(this);

    public Sidebar(int x, int y, int width, int height) {
        init(x, y, width, height);
    }

    @Override
    public void init() {
        expandButton.init(x + 4, y + 4);
        chatButton.init(x + 10, y + 35, width - 20, 21);
        int chatListY = y + 35 + 21 + 10;
        chatList.init(x, chatListY, width, y + height - chatListY);
    }

    public void toggle() {
        isExpanded = !isExpanded;
        init();
        ChatScreen.updateDisplay(false, false);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (isExpanded) {
            int color = ColorUtils.applyBgOpacity(0xFF141519);
            Renderer.drawRect(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), color);
            Renderer.drawBorder(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), 2, 0x10FFFFFF);
            context.drawVerticalLine(x + width, y, y + height - 1, 0x80000000);
            Renderer.drawCenteredText(context, "Chats", x + (width / 2), y + 10);

            chatButton.render(context, mouseX, mouseY);
            chatList.renderWidget(context, mouseX, mouseY, deltaTicks);
        }

        expandButton.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (expandButton.isHovered()) {
            toggle();
            return true;
        } else if (chatButton.isHovered()) {
            ChatManager.getInstance().createChat();
            disableFocus();
            return true;
        }

        return chatList.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return chatList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return chatList.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        return chatList.mouseReleased(click);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!isExpanded && expandButton.isHovered()) return true;
        return isExpanded && super.isHovered();
    }

    public ChatList getChatList() {
        return chatList;
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
