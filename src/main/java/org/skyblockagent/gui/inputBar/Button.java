package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.function.Consumer;

public class Button {
    private static final int FILL_COLOR = 0xFF1B1C1D;
    private static final int OUTLINE_COLOR = 0xFF424447;
    private static final float OUTLINE_THICKNESS = 0.5f;
    protected static final int BUTTON_HEIGHT = 16;
    private static final int CORNER_RADIUS = 8;
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final int width, height;
    private final String label;
    private final Consumer<Boolean> consumer;
    private int x, y;
    private boolean isToggled;

    public Button(String label, boolean defaultValue, Consumer<Boolean> consumer) {
        this.width = textRenderer.getWidth(label) + 10;
        this.label = label;
        this.height = BUTTON_HEIGHT;
        this.isToggled = defaultValue;
        this.consumer = consumer;
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void onClick() {
        isToggled = !isToggled;
        consumer.accept(isToggled);
    }

    protected void render(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawRoundedRect(context, x, y, width - 1, height, CORNER_RADIUS, FILL_COLOR, OUTLINE_THICKNESS, OUTLINE_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, label, x + (width / 2), y + (height / 2) - 4, 0xA0A0A0);

        if (isHovered(mouseX, mouseY) || isToggled) {
            RenderUtils.drawRoundedRect(context, x, y, width - 1, height, CORNER_RADIUS, 0x30FFFFFF);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
