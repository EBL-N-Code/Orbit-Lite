package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.session.Chat;
import org.skyblockagent.managers.session.ChatLoader;
import org.skyblockagent.managers.session.DateGroup;
import org.skyblockagent.managers.session.Line;
import org.skyblockagent.utils.TextUtils;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatList {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final int LINE_HEIGHT = 19;
    private final Scrollbar scrollbar = new Scrollbar();
    private double scrollPercentage = 0f;
    private float scrollOffset = 0;

    private int hoveredLine = -1;

    private int x, y, width, height;

    public void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
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

            int color = isTitle ? 0xFFAFAFAF : 0xFFEEEEEE;
            String text = isTitle ? line.group().getName() : line.chat().getTitle();

            context.drawText(textRenderer, TextUtils.trimString(text, width - 22), x + 12, renderY, color, false);
        }

        context.getMatrices().pop();
        context.disableScissor();

        scrollbar.render(context, mouseX, mouseY);
    }

    public void updateScrollBar() {
        int maxOffset = ChatLoader.getMaxOffset();
        int minY = y;
        int maxY = y + height - 10;

        scrollbar.init(minY, maxY, x + width);

        boolean isScrollable = maxOffset > height;
        scrollbar.setVisibility(isScrollable);

        if (isScrollable) {
            float maxScroll = maxOffset - height;
            scrollPercentage = MathHelper.clamp(scrollPercentage, 0.0f, 1.0f);
            scrollOffset = MathHelper.clamp((float) scrollPercentage * maxScroll, 0.0f, maxScroll);
            scrollbar.update(height, maxOffset, scrollPercentage);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (scrollbar.isHovered(mouseX, mouseY)) {
            scrollbar.setDragging(true);
            return true;
        } else if (hoveredLine != -1) {
            onChatClick();
            return true;
        }
        return false;
    }

    public void onChatClick() {
        UUID chatId = ChatLoader.getLines().get(hoveredLine).chat().getId();
        ChatManager.INSTANCE.loadSession(chatId);
        ChatScreen.INSTANCE.getSidebar().setSwitchedFocus();
    }

    public boolean mouseScrolled(double verticalAmount) {
        if (scrollbar.isVisible()) {
            float scrollDelta = (float) verticalAmount * 55f / (ChatLoader.getMaxOffset() - height);
            scrollPercentage = MathHelper.clamp(scrollPercentage - scrollDelta, 0.0f, 1.0f);
            updateScrollBar();
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double deltaY) {
        if (scrollbar.isDragging()) {
            int maxOffset = ChatLoader.getMaxOffset();
            int minY = scrollbar.getMinY();
            int maxY = scrollbar.getMaxY();
            int barHeight = scrollbar.getHeight();

            double newBarY = MathHelper.clamp(scrollbar.getY() + deltaY, minY, maxY - barHeight);
            scrollPercentage = (newBarY - minY) / ((maxY - minY) - barHeight);
            scrollOffset = (float) (scrollPercentage * (maxOffset - height));

            scrollbar.update(height, maxOffset, scrollPercentage);
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        if (scrollbar.isDragging()) {
            scrollbar.setDragging(false);
            return true;
        }
        return false;
    }
}