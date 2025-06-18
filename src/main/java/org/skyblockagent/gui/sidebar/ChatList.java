package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.chat.Chat;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.chat.DateGroup;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Scrollbar;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatList extends Widget {
    private final Scrollbar scrollbar = new Scrollbar();
    private Chat hoveredChat;

    @Override
    protected void init() {
        scrollbar.init(y, height, x + width, ChatStorage.getInstance().getMaxOffset());
    }

    public void onChatClick() {
        ChatManager.getInstance().loadChat(hoveredChat.getId());
        ChatScreen.getInstance().getSidebar().disableFocus();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float scrollOffset = scrollbar.getScrollOffset();
        context.enableScissor(x, y, x + width, y + height - 3);
        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        Map<DateGroup, List<Chat>> groupedChats = ChatStorage.getInstance().getGroupedChats();
        UUID currentChatId = ChatManager.getInstance().getChat().getId();
        hoveredChat = null;
        int currentY = y;

        for (DateGroup group : DateGroup.values()) {
            List<Chat> chats = groupedChats.get(group);
            if (chats == null || chats.isEmpty()) continue;

            // Render group title
            int logicalY = currentY - (int) scrollOffset;
            if (logicalY + 19 >= y && logicalY <= y + height) {
                RenderUtils.drawText(context, TextUtils.trimString(group.getName(), width - 22), x + 12, currentY, 0xFFAFAFAF);
            }
            currentY += 19; // LINE_HEIGHT

            // Render chats in group
            for (Chat chat : chats) {
                logicalY = currentY - (int) scrollOffset;
                if (currentY + 19 < y + scrollOffset) {
                    currentY += 19;
                    continue;
                }
                if (currentY > y + height + scrollOffset) break;

                boolean isSelected = currentChatId != null && currentChatId.equals(chat.getId());
                boolean isHovered = !scrollbar.isDragging()
                        && mouseY >= logicalY - 6
                        && mouseY < logicalY + 19 - 6
                        && mouseX >= x + 10
                        && mouseX < x + width - 10;
                if (isHovered) hoveredChat = chat;

                if (isHovered || isSelected) {
                    int bgColor = isSelected ? 0xFF202227 : 0xFF2a2d32;
                    RenderUtils.drawRect(context, x + 8, currentY - 6, width - 18, 20, 6, bgColor);
                }

                RenderUtils.drawText(context, TextUtils.trimString(chat.getTitle(), width - 22), x + 12, currentY);
                currentY += 19; // LINE_HEIGHT
            }

            if (group != DateGroup.OLDER) {
                currentY += 10; // Extra spacing for non-OLDER groups
            }
        }

        context.getMatrices().pop();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollbar.mouseClicked(mouseX, mouseY)) {
            return true;
        } else if (hoveredChat != null) {
            onChatClick();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return scrollbar.mouseDragged(deltaY, ChatStorage.getInstance().getMaxOffset(), height);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return scrollbar.mouseReleased();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollbar.mouseScrolled(verticalAmount, ChatStorage.getInstance().getMaxOffset(), height);
    }
}