package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.joml.Vector4f;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.gui.sidebar.Sidebar;
import org.skyblockagent.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatScreen extends Screen {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static ChatScreen INSTANCE;
    private static final int CHAT_WIDTH = 520;
    private static final int CHAT_HEIGHT = 320;
    private static final int HEADER_HEIGHT = 25;
    private final List<Runnable> postRenderQueue = new ArrayList<>();
    private int x, y, menuWidth, menuHeight;
    private Header header;
    private InputBar inputBar;
    private Sidebar sidebar;
    private ChatDisplay chatDisplay;

    public ChatScreen() {
        super(Text.empty());
        INSTANCE = this;
    }

    public static void updateSidebar() {
        if (INSTANCE != null) INSTANCE.sidebar.init();
    }

    public void initializeDisplay(boolean focusOnBar, boolean resetPosition) {
        int displayWidth = (int) (getChatWidth() * (sidebar.isExpanded() ? 0.8f : 0.65f));
        int displayX = getLeft() + (getChatWidth() - displayWidth) / 2;
        chatDisplay.init(displayX, getTop(), displayWidth, getChatHeight());
        if (resetPosition) chatDisplay.getScrollbar().reset();

        int barHeight = 10 + (8 * 2);
        inputBar.init(displayX, getBottom() - barHeight - InputBar.Y_OFFSET, displayWidth, barHeight);
        if (focusOnBar) setFocused(inputBar);

        header.init(x, y, menuWidth, HEADER_HEIGHT);
    }

    public static void updateDisplay(boolean focusOnBar, boolean resetPosition) {
        if (INSTANCE != null) INSTANCE.initializeDisplay(focusOnBar, resetPosition);
    }

    @Override
    protected void init() {
        INSTANCE = this;
        menuWidth = Math.min(CHAT_WIDTH, this.width - 24);
        menuHeight = Math.min(CHAT_HEIGHT, this.height - 12);
        x = (this.width - menuWidth) / 2;
        y = (this.height - menuHeight) / 2;

        header = new Header();
        sidebar = new Sidebar(x, getTop(), (int) (menuWidth * 0.25f), getChatHeight());
        chatDisplay = new ChatDisplay();
        inputBar = new InputBar();

        addDrawableChild(sidebar);
        addDrawableChild(chatDisplay);
        addDrawableChild(inputBar);
        addDrawableChild(header);

        initializeDisplay(true, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Renderer.drawRect(context, getLeft(), y + HEADER_HEIGHT, getChatWidth(), getChatHeight(), new Vector4f(0f, 8f, 0f, sidebar.isExpanded() ? 0f : 8f), 0xFF141519);
        Renderer.drawRect(context, x, y, menuWidth, HEADER_HEIGHT, new Vector4f(8f, 0f, 8f, 0f), 0xFF1F2227);

        // Borders
        context.drawHorizontalLine(x, x + menuWidth - 1, y + 25, 0xFF000000);
        Renderer.drawBorder(context, x + 1, y + 1, menuWidth - 2, menuHeight - 2, 7f, 2f, 0x10FFFFFF);
        Renderer.drawBorder(context, x, y, menuWidth, menuHeight, 8f, 1f, 0xFF000000);

        super.render(context, mouseX, mouseY, delta);

        for (Runnable task : postRenderQueue) {
            task.run();
        }
        postRenderQueue.clear();
    }

    @Override
    public void tick() {
        if (inputBar != null) inputBar.autoScroll();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderDarkening(context);
    }

    public void playDownSound() {
        SoundManager soundManager = CLIENT.getSoundManager();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void removed() {
        INSTANCE = null;
    }

    public int getChatWidth() {
        return sidebar.isExpanded() ? menuWidth - sidebar.getWidth() : menuWidth;
    }

    public int getChatHeight() {
        return menuHeight - HEADER_HEIGHT;
    }

    public int getLeft() {
        return sidebar.isExpanded() ? x + sidebar.getWidth() : x;
    }

    public int getRight() {
        return x + menuWidth;
    }

    public int getTop() {
        return y + HEADER_HEIGHT;
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

    public void queuePostRender(Runnable renderTask) {
        postRenderQueue.add(renderTask);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (header.dropdownClicked(click.x(), click.y()) ||
                (sidebar.isExpanded() && sidebar.getChatList().dropdownClicked(click.x(), click.y()))) {
            playDownSound();
            setFocused(null);
            return true;
        }
        Optional<Element> optional = this.hoveredElement(click.x(), click.y());
        if (optional.isPresent()) {
            Element element = optional.get();
            if (element.mouseClicked(click, doubled)) {
                playDownSound();
                if (click.button() == 0) this.setDragging(true);
                if (((Widget) element).shouldFocus()) this.setFocused(element);
                return true;
            }
        }

        setFocused(null);
        return true;
    }

    public static void openScreen() {
        CLIENT.send(() -> CLIENT.setScreen(new ChatScreen()));
    }

    public static ChatScreen getInstance() {
        return INSTANCE;
    }
}