package org.skyblockagent.gui.header;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.utils.render.RenderUtils;

public class UpgradeButton extends Button {

    @Override
    public void onClick() {

    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawRect(context, x, y, width, height, 4, 0xFF1C1F24, 1f, 0x80000000);
    }
}
