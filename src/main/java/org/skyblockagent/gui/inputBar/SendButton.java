package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.utils.render.RenderUtils;

public class SendButton {
    private static final Identifier ARROW_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/arrow.png");
    protected static final int SIZE = 16;
    private int x, y;

    protected void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected void onClick() {
        ChatManager.INSTANCE.getSession().send();
    }

    protected void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawEllipse(context, x, y, SIZE, SIZE, 0xFFFFFFFF);

        if (ChatManager.INSTANCE.hasActiveRequest()) {
            RenderUtils.drawRect(context, x + 5, y + 5, 6, 6, 0, 0xFF000000);
        } else {
            context.drawTexture(RenderLayer::getGuiTextured, ARROW_TEXTURE, x + 4, y + 4, 0, 0, 8, 8, 8, 8);

            if (isHovered(mouseX, mouseY) || ChatScreen.INSTANCE.getInputBar().textEditor.isEmpty()) {
                RenderUtils.drawEllipse(context, x, y, SIZE, SIZE, 0x60000000);
            }
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + SIZE && mouseY >= y && mouseY <= y + SIZE;
    }
}
