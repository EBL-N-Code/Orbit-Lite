package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.render.RenderUtils;

public class ExpandButton extends Button {
    private static final int SIZE = 19;

    public ExpandButton() {
        super(SIZE, SIZE);
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        if (isHovered(mouseX, mouseY)) {
            RenderUtils.drawEllipse(context, x, y, width, height, 0x20FFFFFF);
        }

        context.drawHorizontalLine(x + 6, x + 12, y + 7, 0xFFFFFFFF);
        context.drawHorizontalLine(x + 6, x + 12, y + 9, 0xFFFFFFFF);
        context.drawHorizontalLine(x + 6, x + 12, y + 11, 0xFFFFFFFF);
    }
}
