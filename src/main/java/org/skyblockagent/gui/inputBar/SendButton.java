package org.skyblockagent.gui.inputBar;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Button;
import org.skyblockagent.network.BackendService;
import org.skyblockagent.render.Renderer;

public class SendButton extends Button {
    private static final Identifier ARROW_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/arrow.png");
    protected static final int SIZE = 16;

    public SendButton() {
        super(SIZE, SIZE);
    }

    @Override
    public void renderButton(DrawContext context) {
        Renderer.drawCircle(context, x, y, width, height, 0xFFFFFFFF);

        if (BackendService.hasActiveCall()) {
            Renderer.drawRect(context, x + 5, y + 5, 6, 6, 0, 0xFF000000);
        } else {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, x + 4, y + 4, 0, 0, 8, 8, 8, 8);
            if (!ChatScreen.getInstance().getInputBar().canSend()) {
                if (isHovered) context.setCursor(StandardCursors.ARROW);
                Renderer.drawCircle(context, x, y, width, height, 0x80000000);
                return;
            }
        }

        if (isHovered) Renderer.drawCircle(context, x, y, width, height, 0x60000000);
    }
}
