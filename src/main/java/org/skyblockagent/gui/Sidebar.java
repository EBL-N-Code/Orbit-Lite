package org.skyblockagent.gui;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

public class Sidebar extends Widget {
    private static boolean collapsed = false;
    private int iconX, iconY;

    public Sidebar(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void init() {
        iconX = collapsed ? x + 10 : x + width - 16;
        iconY = y + 11;
    }

    public void toggle() {
        collapsed = !collapsed;
        init();
        ChatScreen.INSTANCE.initializeDisplay();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (!collapsed) {
            context.drawVerticalLine(x + width, y, y + height - 1, 0x80000000);
        }

        renderIcon(context, mouseX, mouseY);
    }

    private void renderIcon(DrawContext context, double mouseX, double mouseY) {
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
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (collapsed && isIconHovered(mouseX, mouseY)) return true;
        return !collapsed && super.isHovered();
    }

    private boolean isIconHovered(double mouseX, double mouseY) {
        return mouseX >= iconX - 6 && mouseX <= iconX + 12 && mouseY >= iconY - 7 && mouseY <= iconY + 12;
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
