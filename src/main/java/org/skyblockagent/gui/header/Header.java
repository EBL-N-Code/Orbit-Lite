package org.skyblockagent.gui.header;

import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.utils.render.RenderUtils;

public class Header extends Widget {
    private final SettingsButton settingsButton = new SettingsButton();
    private final UpgradeButton upgradeButton = new UpgradeButton();
    private String title;
    private int titleX;

    @Override
    public void init() {
        int right = x + width - 8;
        int buttonHeight = height - (4 * 2);
        settingsButton.init(right - 40, y + 5, 40, buttonHeight);

        right -= 40 + 8;
        upgradeButton.init(right - 40, y + 5, 40, buttonHeight);

        title = ChatManager.INSTANCE.getSession().getTitle();
        if (title != null) {
            titleX = ChatScreen.INSTANCE.getLeft() + (ChatScreen.INSTANCE.getChatWidth() / 2);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        RenderUtils.drawRect(context, x, y, width, height, new Vector4f(8f, 0f, 8f, 0f), 0xFF1F2227);

        settingsButton.render(context, mouseX, mouseY);
        upgradeButton.render(context, mouseX, mouseY);

        if (title != null) {
            RenderUtils.drawCenteredText(context, title, titleX, y + 9);
        }
    }
}
