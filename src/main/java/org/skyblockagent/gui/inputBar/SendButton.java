package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.network.BackendService;
import org.skyblockagent.render.RenderUtils;

public class SendButton extends Button {
    private static final Identifier ARROW_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/arrow.png");
    protected static final int SIZE = 16;

    public SendButton() {
        super(SIZE, SIZE);
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY) {
        RenderUtils.drawEllipse(context, x, y, width, height, 0xFFFFFFFF);

        //Streamer.isActive()
        if (BackendService.hasActiveCall()) {
            RenderUtils.drawRect(context, x + 5, y + 5, 6, 6, 0, 0xFF000000);
        } else {
            context.drawTexture(RenderLayer::getGuiTextured, ARROW_TEXTURE, x + 4, y + 4, 0, 0, 8, 8, 8, 8);
            if (!ChatScreen.getInstance().getInputBar().canSend()) {
                RenderUtils.drawEllipse(context, x, y, width, height, 0x80000000);
                return;
            }
        }

        if (isHovered(mouseX, mouseY)) {
            RenderUtils.drawEllipse(context, x, y, width, height, 0x60000000);
        }
    }
}
