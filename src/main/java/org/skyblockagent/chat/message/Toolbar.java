package org.skyblockagent.chat.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Tooltip;
import org.skyblockagent.network.RunInfo;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.*;

public class Toolbar {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final Identifier COPY_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/copy.png");
    private static final Identifier RETRY_TEXTURE = Identifier.of(Skyblockagent.NAMESPACE, "textures/ui/retry.png");
    private static final int ICON_SIZE = 8;
    private final ChatMessage message;
    private final int x, y;
    private int textWidth;
    private String timeText;
    private Tooltip tooltip;
    private String hoveredIcon;

    public Toolbar(ChatMessage message) {
        this.message = message;
        boolean isAI = message.getSender() == Sender.AI;
        x = isAI ? message.getX() : message.getX() + message.getWidth() - ICON_SIZE - 6;
        y = message.getY() + message.getHeight() - (isAI ? 14 : 8);
        if (isAI) {
            RunInfo runInfo = ((AIMessage) message).getRunInfo();
            if (runInfo != null) {
                timeText = String.format("%.2f", runInfo.totalTime()) + "s";
                textWidth = TEXT_RENDERER.getWidth(timeText) + 6;
                tooltip = new Tooltip(makeTooltip(runInfo));
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float scrollOffset, boolean isLast) {
        boolean isOverBar = ChatScreen.getInstance().getInputBar().isHovered();
        int x = this.x;
        hoveredIcon = null;

        renderIcon(context, COPY_TEXTURE, x, y, mouseX, mouseY, scrollOffset, isOverBar);
        x += ICON_SIZE + 7;

        if (isLast) {
            renderIcon(context, RETRY_TEXTURE, x, y, mouseX, mouseY, scrollOffset, isOverBar);
            x += ICON_SIZE + 7;
        }

        if (tooltip != null) {
            RenderUtils.drawText(context, timeText, x, y, 0xFF808080);
            if (!isOverBar && isTextHovered(x, y, textWidth, mouseX, mouseY, scrollOffset)) {
                RenderUtils.drawRect(context, x - 3, y - 3, textWidth, 14, 6, 0x15FFFFFF);
                tooltip.render(context, x + textWidth, y + 6);
            }
        }
    }

    public void renderIcon(DrawContext context, Identifier texture, int x, int y, int mouseX, int mouseY, float scrollOffset, boolean isOverBar) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        if (!isOverBar && isIconHovered(x, y, mouseX, mouseY, scrollOffset)) {
            RenderUtils.drawEllipse(context, x - 3, y - 3, 14, 14, 0x15FFFFFF);
            hoveredIcon = texture.getPath();
        }
    }

    public boolean mouseClicked() {
        if (hoveredIcon != null) {
            ChatScreen.getInstance().playDownSound();
            if (hoveredIcon.equals(COPY_TEXTURE.getPath())) {
                MinecraftClient.getInstance().keyboard.setClipboard(message.getContent());
            } else {
                ChatManager.getInstance().getChat().retry();
            }
            return true;
        }

        return false;
    }

    public LinkedHashMap<String, String> makeTooltip(RunInfo runInfo) {
        LinkedHashMap<String, String> lines = new LinkedHashMap<>();
        lines.put("Prompt tokens:", String.valueOf(runInfo.promptTokens()));
        lines.put("Completion tokens:", String.valueOf(runInfo.completionTokens()));
        lines.put("Total tokens:", String.valueOf(runInfo.totalTokens()));
        lines.put("TPS:", String.format("%.2f", runInfo.tps()));
        return lines;
    }

    public boolean isIconHovered(int x, int y, int mouseX, int mouseY, float scrollOffset) {
        return mouseX >= x - 3 && mouseX <= x + ICON_SIZE + 3 && mouseY >= y - 3 - scrollOffset && mouseY <= y + ICON_SIZE + 3 - scrollOffset;
    }

    public boolean isTextHovered(int x, int y, int width, int mouseX, int mouseY, float scrollOffset) {
        return mouseX >= x - 3 && mouseX <= x + width - 3 && mouseY >= y - 3 - scrollOffset && mouseY <= y + 11 - scrollOffset;
    }
}
