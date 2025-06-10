package org.skyblockagent.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

public class Button {
    private static final int FILL_COLOR = 0xFF1C1F24;
    private static final int OUTLINE_COLOR = 0x80000000;
    private static final int HOVER_COLOR = 0x15FFFFFF;
    protected int x, y, width, height;
    private String label;

    public Button(String label) {
        this.label = label;
    }

    public Button(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawRect(context, x, y, width, height, 4, FILL_COLOR, 1f, OUTLINE_COLOR);
        RenderUtils.drawCenteredText(context, label, x + (width / 2), y + (height / 2) - 3);

        if (isHovered(mouseX, mouseY)) {
            RenderUtils.drawRect(context, x, y, width, height, 4, HOVER_COLOR);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
