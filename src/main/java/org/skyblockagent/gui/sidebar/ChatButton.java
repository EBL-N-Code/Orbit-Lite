package org.skyblockagent.gui.sidebar;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.utils.render.RenderUtils;

public class ChatButton extends Button {

    @Override
    public void onClick() {
        ChatManager.INSTANCE.createSession();
        ChatScreen.INSTANCE.getSidebar().setSwitchedFocus();
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawRect(context, x, y, width, height, 4, 0xFF1C1F24, 1f, 0x80000000);
        RenderUtils.drawCenteredText(context, "+ New chat", x + (width / 2), y + 7);

        if (isHovered(mouseX, mouseY)) {
            RenderUtils.drawRect(context, x, y, width, height, 4, 0x15FFFFFF);
        }
    }
}
