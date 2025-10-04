package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.render.RenderUtils;

public class Sidebar extends Widget {
    private static boolean isExpanded = true;
    private final ExpandButton expandButton = new ExpandButton();
    private final Button chatButton = new Button("+ New chat");
    private final ChatList chatList = new ChatList();

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
        ChatScreen.initDisplay(false, false);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (isExpanded) {
            RenderUtils.drawRect(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), 0xFF141519);
            RenderUtils.drawBorder(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), 2, 0x10FFFFFF);
            context.drawVerticalLine(x + width, y, y + height - 1, 0x80000000);
            RenderUtils.drawCenteredText(context, "Chats", x + (width / 2), y + 10);

            chatButton.render(context, mouseX, mouseY);
            chatList.renderWidget(context, mouseX, mouseY, deltaTicks);
        }

        expandButton.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expandButton.isHovered(mouseX, mouseY)) {
            toggle();
            return true;
        } else if (chatButton.isHovered(mouseX, mouseY)) {
            ChatManager.getInstance().createChat();
            disableFocus();
            return true;
        }

        return chatList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return chatList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return chatList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return chatList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!isExpanded && expandButton.isHovered(mouseX, mouseY)) return true;
        return isExpanded && super.isHovered();
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
