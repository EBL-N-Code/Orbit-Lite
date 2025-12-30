package org.apex.gui.header;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import org.apex.gui.ChatScreen;
import org.apex.gui.components.Widget;
import org.apex.render.Renderer;

public class ModelManagerWidget extends Widget {

    public ModelManagerWidget() {
        width = 250;
        height = 200;
        x = (ChatScreen.getInstance().width - width) / 2;
        y = (ChatScreen.getInstance().height - height) / 2;
    }

    @Override
    protected void init() {
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Renderer.drawRect(context, x, y, width, height, 1f, 0xFF1F2227);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        ChatScreen.getInstance().closeWindow(this);
        return true;
    }
}
