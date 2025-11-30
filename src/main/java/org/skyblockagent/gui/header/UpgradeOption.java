package org.skyblockagent.gui.header;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.dropdown.ClickableOption;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.ColorUtils;

public class UpgradeOption extends ClickableOption {
    private final static String URL = "https://Upgrade";
    private final static int HEIGHT = 17;
    private final int color;

    public UpgradeOption(String label, int color) {
        super(label, () -> ConfirmLinkScreen.open(ChatScreen.getInstance(), URL));
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
        width -= 8;
        x += 4;

        int colorWithOpacity = ColorUtils.applyOpacity(color, 60);
        Renderer.drawRect(context, x, y, width, HEIGHT, 4, colorWithOpacity, color, 1f);
        Renderer.drawCenteredText(context, label, x + (width / 2), y + (HEIGHT / 2) - 3, 0xFFFFFFFF);

        if (isHovered) {
            Renderer.drawRect(context, x, y, width, HEIGHT, 4, 0x15FFFFFF);
        }
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}