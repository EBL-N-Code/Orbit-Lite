package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Scrollbar;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.session.ChatLoader;
import org.skyblockagent.chat.session.Line;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;
import java.util.UUID;

public class ChatList extends Widget {
    private static final int LINE_HEIGHT = 19;
    private final Scrollbar scrollbar = new Scrollbar(0f, 0);
    private int hoveredLine = -1;

    @Override
    protected void init() {
        scrollbar.init(y, height, x + width, ChatLoader.getMaxOffset());
    }

    public void onChatClick() {
        UUID chatId = ChatLoader.getLines().get(hoveredLine).chat().getId();
        ChatManager.INSTANCE.loadSession(chatId);
        ChatScreen.INSTANCE.getSidebar().setSwitchedFocus();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float scrollOffset = scrollbar.getScrollOffset();
        context.enableScissor(x, y, x + width, y + height - 3);
        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        List<Line> lines = ChatLoader.getLines();
        hoveredLine = -1;

        int groupsCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean isTitle = line.isGroupTitle();
            if (isTitle && i != 0) groupsCount++;
            int renderY = y + (i * LINE_HEIGHT) + (groupsCount * 10);
            int logicalY = renderY - (int) scrollOffset;

            if (renderY + LINE_HEIGHT < y + scrollOffset) continue;
            if (renderY > y + height + scrollOffset) break;

            UUID currentSessionId = ChatManager.INSTANCE.getSession().getId();
            boolean isSelected = !isTitle && currentSessionId != null && currentSessionId.equals(line.chat().getId());
            boolean isHovered = !scrollbar.isDragging()
                    && !isTitle
                    && mouseY >= logicalY - 6
                    && mouseY < logicalY + LINE_HEIGHT - 6
                    && mouseX >= x + 10
                    && mouseX < x + width - 10;
            if (isHovered) hoveredLine = i;

            if (isHovered || isSelected) {
                int bgColor = isSelected ? 0xFF202227 : 0xFF2a2d32;
                RenderUtils.drawRect(context, x + 8, renderY - 6, width - 18, 20, 6, bgColor);
            }

            int color = isTitle ? 0xFFAFAFAF : 0xFFE0E0E0;
            String text = isTitle ? line.group().getName() : line.chat().getTitle();

            RenderUtils.drawText(context, TextUtils.trimString(text, width - 22), x + 12, renderY, color);
        }

        context.getMatrices().pop();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollbar.mouseClicked(mouseX, mouseY)) {
            return true;
        } else if (hoveredLine != -1) {
            onChatClick();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return scrollbar.mouseDragged(deltaY, ChatLoader.getMaxOffset(), height);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return scrollbar.mouseReleased();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollbar.mouseScrolled(verticalAmount, ChatLoader.getMaxOffset(), height);
    }
}