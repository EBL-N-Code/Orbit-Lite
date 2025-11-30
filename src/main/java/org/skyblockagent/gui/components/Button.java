package org.skyblockagent.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.ColorUtils;

public class Button {
    protected boolean isHovered;
    protected int x, y, width, height;
    protected String label;

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
        isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        if (isHovered) context.setCursor(StandardCursors.POINTING_HAND);
        renderButton(context);
    }

    public void renderButton(DrawContext context) {
        int color = ColorUtils.applyBgOpacity(0xFF1C1F24);
        Renderer.drawRect(context, x, y, width, height, 4, color, 0x80000000, 1f);
        Renderer.drawCenteredText(context, label, x + (width / 2), y + (height / 2) - 3);

        if (isHovered) {
            Renderer.drawRect(context, x, y, width, height, 4, 0x15FFFFFF);
        }
    }

    public boolean isHovered() {
        return isHovered;
    }
}
