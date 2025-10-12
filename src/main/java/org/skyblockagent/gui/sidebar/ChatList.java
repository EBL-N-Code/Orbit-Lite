package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatSummary;
import org.skyblockagent.chat.DateGroup;
import org.skyblockagent.gui.components.Scrollbar;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.gui.components.dropdown.Dropdown;
import org.skyblockagent.network.BackendService;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatList extends Widget {
    private final Scrollbar scrollbar = new Scrollbar();
    private final Sidebar sidebar;
    private ChatSummary hoveredChat;
    private boolean isOptionsButtonHovered;
    private Dropdown dropdown;
    private int dropdownX, dropdownY;

    public ChatList(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    @Override
    protected void init() {
        scrollbar.init(y, height, x + width, getMaxOffset());
    }

    public int getMaxOffset() {
        Map<DateGroup, List<ChatSummary>> groupedChats = BackendService.getChatSummaries();
        return Arrays.stream(DateGroup.values())
                .mapToInt(group -> {
                    List<ChatSummary> chats = groupedChats.get(group);
                    if (chats == null) return 0;
                    int size = chats.size();
                    if (size == 0) return 0;
                    int offset = 19 + size * 19;
                    if (group != DateGroup.OLDER) offset += 10;
                    return offset;
                })
                .sum();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float scrollOffset = scrollbar.getScrollOffset();
        int bottom = y + height - 3;
        context.enableScissor(x, y, x + width, bottom);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -scrollOffset);

        Map<DateGroup, List<ChatSummary>> groupedChats = BackendService.getChatSummaries();
        String currentChatId = ChatManager.getInstance().getChat().getUuid();
        hoveredChat = null;
        isOptionsButtonHovered = false;
        int currentY = y;

        for (DateGroup group : DateGroup.values()) {
            List<ChatSummary> chats = groupedChats.get(group);
            if (chats == null || chats.isEmpty()) continue;

            // Render group title
            int logicalY = currentY - (int) scrollOffset;
            if (logicalY + 19 >= y && logicalY <= y + height) {
                Renderer.drawText(context, TextUtils.trimString(group.getName(), width - 22), x + 12, currentY, 0xFFAFAFAF);
            }
            currentY += 19; // LINE_HEIGHT

            // Render chats in group
            for (ChatSummary chat : chats) {
                logicalY = currentY - (int) scrollOffset;
                if (currentY + 19 < y + scrollOffset) {
                    currentY += 19;
                    continue;
                }
                if (currentY > y + height + scrollOffset) break;

                boolean isDropdownHovered = dropdown != null && dropdown.isHovered(mouseX, mouseY + scrollOffset);
                boolean isSelected = currentChatId != null && currentChatId.equals(chat.uuid());
                boolean isHovered = !isDropdownHovered
                        && !scrollbar.isDragging()
                        && mouseY >= y && mouseY <= bottom
                        && mouseY >= logicalY - 6
                        && mouseY < logicalY + 19 - 6
                        && mouseX >= x + 10
                        && mouseX < x + width - 10;

                if (isHovered || isSelected) {
                    int bgColor = isHovered ? 0xFF2a2d32 : 0xFF202227;
                    Renderer.drawRect(context, x + 8, currentY - 6, width - 18, 20, 6, bgColor);

                    if (isHovered) {
                        context.setCursor(StandardCursors.POINTING_HAND);

                        hoveredChat = chat;
                        String dots = "...";
                        int dotsWidth = textRenderer.getWidth(dots);
                        int dotsX = x + width - 18 - dotsWidth;
                        float scale = 1.5f;
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(dotsX, currentY - 5);
                        context.getMatrices().scale(scale, scale);
                        Renderer.drawText(context, dots, 0, 0, 0xFFFFFFFF);
                        context.getMatrices().popMatrix();

                        isOptionsButtonHovered = mouseX >= dotsX - 3 &&
                                mouseX < dotsX + (int) (dotsWidth * scale) + 1 &&
                                mouseY >= logicalY - 2 &&
                                mouseY < logicalY + 11;

                        if (isOptionsButtonHovered) {
                            Renderer.drawCircle(context, dotsX - 3, currentY - 2, (int) (dotsWidth * scale) + 4, 13, 0x15FFFFFF);
                            dropdownX = dotsX - 3;
                            dropdownY = currentY + 11;
                        }
                    }
                }

                String chatName = chat.name() == null ? "null" : chat.name();
                Renderer.drawText(context, TextUtils.trimString(chatName, width - 40), x + 12, currentY);
                currentY += 19; // LINE_HEIGHT
            }

            if (group != DateGroup.OLDER) currentY += 10; // Extra spacing for non-OLDER groups
        }

        context.getMatrices().popMatrix();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);

        context.enableScissor(x, y, x + width + 100, bottom);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -scrollOffset);
        if (dropdown != null) dropdown.render(context, mouseX, (int) (mouseY + scrollOffset), deltaTicks);
        context.getMatrices().popMatrix();
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (hoveredChat != null) {
            if (isOptionsButtonHovered) {
                String chatUuid = hoveredChat.uuid();
                dropdown = Dropdown.builder(chatUuid)
                        .position(dropdownX, dropdownY)
                        .addOption("Delete", () -> ChatManager.getInstance().removeChat(chatUuid))
                        .build();
            } else {
                ChatManager.getInstance().loadChat(hoveredChat.uuid());
                sidebar.disableFocus();
            }
            return true;
        }
        return scrollbar.mouseClicked(click);
    }

    public boolean dropdownClicked(double mouseX, double mouseY) {
        if (dropdown == null) return false;
        if (isOptionsButtonHovered && hoveredChat != null && hoveredChat.uuid().equals(dropdown.getIdentifier())) {
            dropdown = null;
            return true;
        }
        if (dropdown.isHovered(mouseX, mouseY, scrollbar.getScrollOffset())) {
            if (dropdown.handleClick()) dropdown = null;
            return true;
        }
        dropdown = null;
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return scrollbar.mouseDragged(deltaY, getMaxOffset(), height);
    }

    @Override
    public boolean mouseReleased(Click click) {
        return scrollbar.mouseReleased();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxOffset = getMaxOffset();
        if (dropdown != null && maxOffset >= height) dropdown = null;
        return scrollbar.mouseScrolled(verticalAmount, maxOffset, height);
    }
}