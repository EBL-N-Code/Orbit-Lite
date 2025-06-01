package org.skyblockagent.gui;

import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.utils.render.RenderUtils;

public class Header extends Widget {

    public Header(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void init() {
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        RenderUtils.drawRect(context, x, y, width, height, new Vector4f(8f, 0f, 8f, 0f), 0xFF1F2227);
    }
}
