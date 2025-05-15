package org.skyblockagent.gui.inputBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;
import org.skyblockagent.utils.render.RenderUtils;

public class SendButton {
    private static final Identifier ARROW_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/arrow.png");
    protected static final int SIZE = 16;
    private final InputBar inputBar;
    private int x, y;

    public SendButton(InputBar inputBar) {
        this.inputBar = inputBar;
    }

    protected void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected void onClick() {
        if (inputBar.textEditor.isEmpty()) return;
        ChatManager.INSTANCE.session.addMessage();
    }

    protected void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawEllipse(context, x, y, SIZE, SIZE, 0xFFFFFFFF);

        context.drawTexture(RenderLayer::getGuiTextured, ARROW_TEXTURE, x + 4, y + 4, 0, 0, 8, 8, 8, 8);

        if (isHovered(mouseX, mouseY) || inputBar.textEditor.isEmpty()) {
            RenderUtils.drawEllipse(context, x, y, SIZE, SIZE, 0x60000000);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + SIZE && mouseY >= y && mouseY <= y + SIZE;
    }
}
