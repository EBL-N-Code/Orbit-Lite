package org.apex.gui.header;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apex.gui.ChatScreen;
import org.apex.gui.components.Selector;
import org.apex.gui.components.dropdown.ClickableOption;
import org.apex.gui.components.dropdown.Dropdown;
import org.apex.render.Renderer;

public class ModelSelector extends Selector {

    public ModelSelector(int x, int y) {
        super(x, y);
    }

    public void renderButton(DrawContext context, int mouseX, int mouseY) {
//        String chatMode = ConfigManager.getState().getChatMode().getName();
        String chatMode = "";
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
                .addClickableOption(new ModelManagerButton());

        dropdown = builder.build();
    }

    public static class ApexModelButton extends ClickableOption {
        public ApexModelButton() {
            super("Apex Model", () -> {});
        }
    }

    public static class ModelManagerButton extends ClickableOption {
        public ModelManagerButton() {
            super("Manage Models", () -> ChatScreen.getInstance().openWindow(new ModelManagerWidget()));
        }
    }
}
