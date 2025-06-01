package org.skyblockagent.gui;

import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.utils.render.RenderUtils;

public class Sidebar extends Widget {
    private static boolean isExpanded = true;
    private int iconX, iconY;
    private int buttonX, buttonY;
    private final ChatList chatList = new ChatList();

    public Sidebar(int x, int y, int width, int height) {
        init(x, y, width, height);
    }

    @Override
    protected void init() {
        iconX = x + 10;
        iconY = y + 11;
        buttonX = x + 10;
        buttonY = y + 35;
        chatList.init(x, buttonY + 21 + 10, width, y + height - (buttonY + 21 + 10));

        chatList.updateScrollBar();
    }

    public void toggle() {
        isExpanded = !isExpanded;
        init();
        ChatScreen.INSTANCE.initializeDisplay(false, false);
    }

    private void renderButton(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawRect(context, buttonX, buttonY, width - 20, 21, 4, 0xFF1C1F24, 1f, 0x80000000);
        context.drawCenteredTextWithShadow(textRenderer, "+ New chat", buttonX + ((width - 20) / 2), buttonY + 7, 0xFFEEEEEE);

        if (isButtonHovered(mouseX, mouseY)) {
            RenderUtils.drawRect(context, buttonX, buttonY, width - 20, 21, 4, 0x15FFFFFF);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (isExpanded) {
            RenderUtils.drawRect(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), 0xFF141519);
            RenderUtils.drawBorder(context, x + 1, y + 1, width - 1, height - 2, new Vector4f(0, 0, 0, 8f), 2, 0x10FFFFFF);
            context.drawVerticalLine(x + width, y, y + height - 1, 0x80000000);
            context.drawCenteredTextWithShadow(textRenderer, "Chats", x + (width / 2), y + 10, 0xFFEEEEEE);

            renderButton(context, mouseX, mouseY);
            chatList.render(context, mouseX, mouseY);
        }

        renderIcon(context, mouseX, mouseY);
    }

    private void onButtonClick() {
        ChatManager.INSTANCE.createSession();
        setSwitchedFocus();
    }

    private void renderIcon(DrawContext context, int mouseX, int mouseY) {
        if (isIconHovered(mouseX, mouseY)) {
            RenderUtils.drawEllipse(context, iconX - 6, iconY - 7, 19, 19, 0x20FFFFFF);
        }

        context.drawHorizontalLine(iconX, iconX + 6, iconY, 0xFFFFFFFF);
        context.drawHorizontalLine(iconX, iconX + 6, iconY + 2, 0xFFFFFFFF);
        context.drawHorizontalLine(iconX, iconX + 6, iconY + 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isIconHovered(mouseX, mouseY)) {
            toggle();
            return true;
        } else if (isButtonHovered(mouseX, mouseY)) {
            onButtonClick();
            return true;
        }

        return chatList.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return chatList.mouseScrolled(verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return chatList.mouseDragged(deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return chatList.mouseReleased();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!isExpanded && isIconHovered(mouseX, mouseY)) return true;
        return isExpanded && super.isHovered();
    }

    private boolean isButtonHovered(double mouseX, double mouseY) {
        return mouseX >= buttonX && mouseX <= buttonX + width - 20 && mouseY >= buttonY && mouseY <= buttonY + 21;
    }

    private boolean isIconHovered(double mouseX, double mouseY) {
        return mouseX >= iconX - 6 && mouseX <= iconX + 12 && mouseY >= iconY - 7 && mouseY <= iconY + 12;
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
