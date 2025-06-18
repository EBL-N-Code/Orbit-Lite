package org.skyblockagent.gui;

import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.utils.render.RenderUtils;

public class Header extends Widget {
    private final Button settingsButton = new Button("Settings");
    private final Button upgradeButton = new Button("Upgrade");
    private String title;
    private int titleX;

    @Override
    public void init() {
        int right = x + width - 8;
        int buttonHeight = height - (4 * 2);
        int buttonWidth = 60;
        settingsButton.init(right - buttonWidth, y + 5, buttonWidth, buttonHeight);

        right -= buttonWidth + 8;
        upgradeButton.init(right - buttonWidth, y + 5, buttonWidth, buttonHeight);

        title = ChatManager.getInstance().getChat().getTitle();
        if (title != null) {
            ChatScreen chatScreen = ChatScreen.getInstance();
            titleX = chatScreen.getLeft() + (chatScreen.getChatWidth() / 2);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        RenderUtils.drawRect(context, x, y, width, height, new Vector4f(8f, 0f, 8f, 0f), 0xFF1F2227);

//        settingsButton.render(context, mouseX, mouseY);
//        upgradeButton.render(context, mouseX, mouseY);

        if (title != null) {
            RenderUtils.drawCenteredText(context, title, titleX, y + 9);
        }
    }
}
