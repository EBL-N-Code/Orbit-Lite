package org.skyblockagent.gui.header;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.skyblockagent.config.ConfigManager;
import org.skyblockagent.gui.components.Selector;
import org.skyblockagent.gui.components.dropdown.ClickableOption;
import org.skyblockagent.gui.components.dropdown.Dropdown;
import org.skyblockagent.render.Renderer;

public class ModelSelector extends Selector {

    public ModelSelector(int x, int y) {
        super(x, y);
    }

    public void renderButton(DrawContext context, int mouseX, int mouseY) {
        String chatMode = ConfigManager.getState().getChatMode().getName();
        int chatModeWidth = MinecraftClient.getInstance().textRenderer.getWidth(chatMode);
        Renderer.drawText(context, chatMode, x + 5, y + 4);

        MutableText arrowHead = Text.literal("⌄").withColor(0xFF7a7f8a);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + chatModeWidth + 8, y - 4);
        context.getMatrices().scale(2f, 2f);
        Renderer.drawText(context, arrowHead, 0, 0);
        context.getMatrices().popMatrix();

        int width = chatModeWidth + 17;
        int height = 16;

        hovered = mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;

        if (hovered) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawRect(context, x, y, width, height, 4f, 0x20FFFFFF);
        }
    }

    @Override
    public void createDropdown() {
        Dropdown.Builder builder = Dropdown.builder("ChatMode").position(x, y + 18, false);

        builder.addClickableOption(new ApexModelButton())
                .addSeparator()
                .addClickableOption(new ClickableOption("Manage Models", () -> {}));

        dropdown = builder.build();
    }

    public static class ApexModelButton extends ClickableOption {
        public ApexModelButton() {
            super("Apex Model", () -> {});
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
            if (isHovered) {
                Renderer.drawRect(context, x + OFFSET, y, width - (OFFSET * 2), HEIGHT, 4, 0x33FFFFFF);
            }

            Renderer.drawText(context, label, x + PADDING, y + 1 + (HEIGHT - textRenderer.fontHeight) / 2);
        }
    }
}
