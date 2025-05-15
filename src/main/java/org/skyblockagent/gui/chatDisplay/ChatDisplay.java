package org.skyblockagent.gui.chatDisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.List;

public class ChatDisplay extends ClickableWidget {
    private int x, y;
    private int maxWidth;
    private int yOffset = 20;

    public ChatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.x = x;
        this.y = y;
        this.maxWidth = (width * 2) / 3;

        for (ChatMessage chatMessage : ChatManager.INSTANCE.session.getChatMessages()) {
            initMessage(chatMessage);
        }
    }

    public void initMessage(ChatMessage chatMessage) {
        chatMessage.setDimentions(maxWidth);
        chatMessage.setPosition(x + width - chatMessage.getWidth(), y + yOffset);
        yOffset += chatMessage.getHeight() + 5;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        List<ChatMessage> chatMessages = ChatManager.INSTANCE.session.getChatMessages();

        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.render(context);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
