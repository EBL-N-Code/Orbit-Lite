package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.skyblockagent.gui.chatDisplay.ChatDisplay;
import org.skyblockagent.gui.inputBar.Button;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.Session;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatScreen extends SafeScreen {
    public static ChatScreen INSTANCE;
    private static final int CHAT_WIDTH = 520;//443
    private static final int CHAT_HEIGHT = 320;//282
    private static final int CHAT_ROUNDNESS = 8;
    private static final int FILL_COLOR = 0xFF1B1C1D;
    private static final int OUTLINE_COLOR = 0xFF4D4D4D;
    private static final float OUTLINE_THICKNESS = 0.5f;
    private final Screen parent;
    private int x, y;
    private int menuWidth, menuHeight;
    private InputBar inputBar;
    private ChatDisplay chatDisplay;

    public ChatScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
        INSTANCE = this;
    }

    @Override
    protected void init() {
        // Main Menu
        menuWidth = Math.min(CHAT_WIDTH, this.width - 24);
        menuHeight = Math.min(CHAT_HEIGHT, this.height - 12);
        x = (this.width - menuWidth) / 2;
        y = (this.height - menuHeight) / 2;

        // Input Bar
        initInputBar();

        // ChatDisplay
        if (!ChatManager.INSTANCE.session.isPreview()) initDisplay();

        setInitialFocus(inputBar);
    }

    public void initInputBar() {
        Session session = ChatManager.INSTANCE.session;

        // Buttons
        List<Button> buttons = new ArrayList<>();
        session.getInputState().getOptions().forEach((k, v) -> {
            Consumer<Boolean> consumer = value -> session.getInputState().setOption(k, value);
            buttons.add(new Button(k, v, consumer));
        });

        // Input Bar
        int barWidth = (int) (menuWidth * 0.7f);
        int barHeight = (int) (menuWidth * 0.1f);
        int barX = x + (menuWidth - barWidth) / 2;
        int barY = y;

        int cornerRadius = Math.min(InputBar.BAR_ROUNDNESS, barHeight / 2);
        int baseHeight = Button.BUTTON_HEIGHT + cornerRadius * 2;
        barHeight = Math.max(barHeight, InputBar.LINE_HEIGHT + baseHeight);

        barY += ChatManager.INSTANCE.session.isPreview() ? (menuHeight - barHeight) / 2 : menuHeight - barHeight - InputBar.Y_OFFSET;
        barHeight += (session.getInputState().getLines().size() - 1) * InputBar.LINE_HEIGHT;

        inputBar = new InputBar(barX, barY, barWidth, barHeight, cornerRadius, baseHeight, buttons);
        addDrawableChild(inputBar);
    }

    public void initDisplay() {
        chatDisplay = new ChatDisplay(inputBar.getX(), y, inputBar.getWidth(), inputBar.getY() - y + inputBar.getCornerRadius());
        addDrawableChild(chatDisplay);
    }

    public void onMessage(boolean firstMessage) {
        inputBar.reset(firstMessage);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);

        RenderUtils.drawRoundedRect(context, x,y, menuWidth, menuHeight, CHAT_ROUNDNESS, FILL_COLOR, OUTLINE_THICKNESS, OUTLINE_COLOR);

        if (ChatManager.INSTANCE.session.isPreview()) {
            renderGreatingText(context, x + (menuWidth / 2), inputBar.getY() - 20);
        }
    }

    public void renderGreatingText(DrawContext context, int x, int y) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0f);
        context.getMatrices().scale(1.5f, 1.5f, 1f);
        context.drawCenteredTextWithShadow(textRenderer, "Good Evening, " + MinecraftClient.getInstance().getGameProfile().getName(), 0, 0, 0xFFFFFF);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!inputBar.isHovered(mouseX, mouseY)) {
            inputBar.setFocused(false);
        } else {
            inputBar.mouseClicked(mouseX, mouseY, button);
            if (button == 0) this.setDragging(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        inputBar.tick();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return menuWidth;
    }

    public int getHeight() {
        return menuHeight;
    }

    public ChatDisplay getChatDisplay() {
        return chatDisplay;
    }
}