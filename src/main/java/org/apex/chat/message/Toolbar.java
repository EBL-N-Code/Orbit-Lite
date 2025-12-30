package org.apex.chat.message;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.util.Identifier;
import org.apex.Apex;
import org.apex.chat.ChatManager;
import org.apex.gui.ChatScreen;
import org.apex.gui.components.Tooltip;
import org.apex.render.Renderer;

import java.util.LinkedHashMap;

public class Toolbar {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final Identifier COPY_TEXTURE = Identifier.of(Apex.NAMESPACE, "textures/ui/copy.png");
    private static final Identifier RETRY_TEXTURE = Identifier.of(Apex.NAMESPACE, "textures/ui/retry.png");
    private static final int ICON_SIZE = 8;
    private final ChatMessage message;
    private final int x, y;
    private int textWidth;
    private String timeText;
    private Tooltip tooltip;
    private String hoveredIcon;

    public Toolbar(ChatMessage message) {
        this.message = message;
        boolean isAI = message.getSender() == Sender.ASSISTANT;
        this.x = isAI ? message.getX() : message.getX() + message.getWidth() - ICON_SIZE - 6;
        this.y = message.getY() + message.getHeight() - (isAI ? 14 : 8);
        if (isAI) {
            if (message instanceof AIMessage aiMessage) {
                AIMessage.UsageInfo usageInfo = aiMessage.getUsage();
                if (usageInfo != null) {
                    timeText = String.format("%.2f", usageInfo.latency() / 1000.0) + "s";
                    textWidth = TEXT_RENDERER.getWidth(timeText) + 6;
                    tooltip = new Tooltip(makeTooltip(usageInfo));
                }
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float scrollOffset, boolean isLast) {
        boolean isInputBarHovered = ChatScreen.getInstance().getInputBar().isHovered();
        int currentX = this.x;
        int offset = 7;
        hoveredIcon = null;

        renderIcon(context, COPY_TEXTURE, currentX, y, mouseX, mouseY, scrollOffset, isInputBarHovered);
        currentX += ICON_SIZE + offset;

        if (isLast) {
            renderIcon(context, RETRY_TEXTURE, currentX, y, mouseX, mouseY, scrollOffset, isInputBarHovered);
            currentX += ICON_SIZE + offset;
        }

        if (tooltip != null) {
            Renderer.drawText(context, timeText, currentX, y, 0xFF808080);
            if (!isInputBarHovered && isTextHovered(currentX, y, textWidth, mouseX, mouseY, scrollOffset)) {
                Renderer.drawRect(context, currentX - 3, y - 3, textWidth, 14, 6, 0x15FFFFFF);
                int tooltipX = currentX + textWidth;
                int tooltipY = y + 6;
                ChatScreen.getInstance().queuePostRender(() -> {
                    context.getMatrices().pushMatrix();
                    context.getMatrices().translate(0, -scrollOffset);
                    tooltip.render(context, tooltipX, tooltipY);
                    context.getMatrices().popMatrix();
                });
            }
        }
    }

    public void renderIcon(DrawContext context, Identifier texture, int x, int y, int mouseX, int mouseY, float scrollOffset, boolean isInputBarHovered) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        if (!isInputBarHovered && isIconHovered(x, y, mouseX, mouseY, scrollOffset)) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawCircle(context, x - 3, y - 3, 14, 14, 0x15FFFFFF);
            hoveredIcon = texture.getPath();
        }
    }

    public boolean mouseClicked() {
        if (hoveredIcon == null) return false;
        if (hoveredIcon.equals(COPY_TEXTURE.getPath())) {
            MinecraftClient.getInstance().keyboard.setClipboard(message.getContent());
        } else if (hoveredIcon.equals(RETRY_TEXTURE.getPath())) {
            ChatManager.getInstance().getChat().retry();
        }

        return true;
    }

    public LinkedHashMap<String, String> makeTooltip(AIMessage.UsageInfo usageInfo) {
        LinkedHashMap<String, String> tooltip = new LinkedHashMap<>();
        tooltip.put("TPS:", String.valueOf(usageInfo.tps()));
        tooltip.put("Input Tokens:", String.valueOf(usageInfo.inputTokens()));
        tooltip.put("Output Tokens:", String.valueOf(usageInfo.outputTokens()));
        tooltip.put("Prompt Tokens:", String.valueOf(usageInfo.promptTokens()));
        tooltip.put("Cached Tokens:", String.valueOf(usageInfo.cachedTokens()));
        tooltip.put("Tokens Usage:", String.valueOf(usageInfo.tokensUsage()));
        return tooltip;
    }

    public boolean isIconHovered(int x, int y, int mouseX, int mouseY, float scrollOffset) {
        return mouseX >= x - 3 && mouseX <= x + ICON_SIZE + 3 && mouseY >= y - 3 - scrollOffset && mouseY <= y + ICON_SIZE + 3 - scrollOffset;
    }

    public boolean isTextHovered(int x, int y, int width, int mouseX, int mouseY, float scrollOffset) {
        return mouseX >= x - 3 && mouseX <= x + width - 3 && mouseY >= y - 3 - scrollOffset && mouseY <= y + 11 - scrollOffset;
    }
}
