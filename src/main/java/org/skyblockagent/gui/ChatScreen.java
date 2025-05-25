package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.joml.Vector4f;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.gui.others.SafeScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.Optional;

public class ChatScreen extends SafeScreen {
    public static ChatScreen INSTANCE;
    private static final int CHAT_WIDTH = 520;
    private static final int CHAT_HEIGHT = 320;
    private final int headerHeight = 25;
    private final Screen parent;
    private int x, y, menuWidth, menuHeight;
    private InputBar inputBar;
    private Sidebar sidebar;
    private ChatDisplay chatDisplay;

    public ChatScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
        INSTANCE = this;
    }

    @Override
    protected void init() {
        menuWidth = Math.min(CHAT_WIDTH, this.width - 24);
        menuHeight = Math.min(CHAT_HEIGHT, this.height - 12);
        x = (this.width - menuWidth) / 2;
        y = (this.height - menuHeight) / 2;

        sidebar = new Sidebar(x, y, (int) (menuWidth * 0.2f), menuHeight);
        chatDisplay = new ChatDisplay();
        inputBar = new InputBar();

        initializeDisplay();

        addDrawableChild(sidebar);
        addDrawableChild(inputBar);
        addDrawableChild(chatDisplay);
        setFocused(inputBar);
    }

    public void initializeDisplay() {
        int displayWidth = (int) (getChatWidth() * (sidebar.isCollapsed() ? 0.65f : 0.8f));
        int displayHeight = getChatHeight();
        int displayX = getLeft() + (getChatWidth() - displayWidth) / 2;
        chatDisplay.init(displayX, getTop() + 1, displayWidth, displayHeight);

        int barHeight = 10 + (8 * 2);
        inputBar.init(displayX, getBottom() - barHeight - InputBar.Y_OFFSET, displayWidth, barHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (ChatManager.INSTANCE.getSession().isEmpty()) {
            renderGreatingText(context, getLeft() + (getChatWidth() / 2), (getChatHeight() / 2) * 3);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);

        RenderUtils.drawRect(context, x, y, menuWidth, 25, new Vector4f(8f, 0f, 8f, 0f), 0xFF1F2227);//0xFF1A1C22
        RenderUtils.drawRect(context, x, y + 25, menuWidth, menuHeight - 25, new Vector4f(0f, 8f, 0f, 8f), 0xFF141519);

        RenderUtils.drawBorder(context, x + 1, y + 1, menuWidth - 2, menuHeight - 2, 7f, 2f, 0x10FFFFFF);
        context.drawHorizontalLine(x, x + menuWidth - 1, y + 25, 0xFF000000);
        RenderUtils.drawBorder(context, x, y, menuWidth, menuHeight, 8f, 1f, 0xFF000000);
    }

    public void renderGreatingText(DrawContext context, int x, int y) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0f);
        context.getMatrices().scale(1.5f, 1.5f, 1f);
        context.drawCenteredTextWithShadow(textRenderer, "Good Evening, " + MinecraftClient.getInstance().getGameProfile().getName(), 0, 0, 0xFFFFFF);
        context.getMatrices().pop();
    }

    @Override
    public void tick() {
        inputBar.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<Element> optional = this.hoveredElement(mouseX, mouseY);
        if (optional.isPresent()) {
            Element element = optional.get();
            if (element.mouseClicked(mouseX, mouseY, button)) {
                playDownSound();
                if (button == 0) this.setDragging(true);
                if (getFocused() != element) this.setFocused(element);
                return true;
            }
        }

        setFocused(null);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!inputBar.isHovered()) {
            if (chatDisplay.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void playDownSound() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    public int getChatWidth() {
        return sidebar.isCollapsed() ? menuWidth : menuWidth - sidebar.getWidth();
    }

    public int getChatHeight() {
        return height - headerHeight;
    }

    public int getLeft() {
        return sidebar.isCollapsed() ? x : x + sidebar.getWidth();
    }

    public int getRight() {
        return x + menuWidth;
    }

    public int getTop() {
        return y + headerHeight;
    }

    public int getBottom() {
        return y + menuHeight;
    }

    public ChatDisplay getChatDisplay() {
        return chatDisplay;
    }

    public InputBar getInputBar() {
        return inputBar;
    }
}