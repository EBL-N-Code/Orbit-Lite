package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.skyblockagent.gui.inputBar.Button;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatScreen extends SafeScreen {
    private static final int CHAT_WIDTH = 443;
    private static final int CHAT_HEIGHT = 282;
    private static final int CHAT_ROUNDNESS = 8;
    private static final int FILL_COLOR = 0xFF1B1C1D;
    private static final int OUTLINE_COLOR = 0xFF4D4D4D;
    private static final float OUTLINE_THICKNESS = 0.5f;
    private static final float QUERY_BAR_WIDTH_PRCT = 0.6f;
    private static final float QUERY_BAR_HEIGHT_PRCT = 0.1f;
    private final Screen parent;
    private int x, y;
    private int menuWidth, menuHeight;
    private InputBar inputBar;
    private boolean search = false;
    private boolean deepSearch = false;

    public ChatScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Main Menu
        menuWidth = Math.min(CHAT_WIDTH, this.width - 24);
        menuHeight = Math.min(CHAT_HEIGHT, this.height - 12);
        x = (this.width - menuWidth) / 2;
        y = (this.height - menuHeight) / 2;

        // Buttons
        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button("Search", search, newValue -> search = newValue));
        buttons.add(new Button("Deep Search", deepSearch, newValue -> deepSearch = newValue));

        // Query Bar
        int queryBarWidth = (int) (menuWidth * QUERY_BAR_WIDTH_PRCT);
        int queryBarHeight = (int) (menuHeight * QUERY_BAR_HEIGHT_PRCT);
        int queryBarX = x + (menuWidth - queryBarWidth) / 2;
        int queryBarY = y + (menuHeight - queryBarHeight) / 2;
        inputBar = new InputBar(queryBarX, queryBarY, queryBarWidth, queryBarHeight, buttons);

        addDrawableChild(inputBar);
        setInitialFocus(inputBar);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);

        RenderUtils.drawRoundedRect(context, x,y, menuWidth, menuHeight, CHAT_ROUNDNESS, FILL_COLOR, OUTLINE_THICKNESS, OUTLINE_COLOR);

        context.getMatrices().push();
        context.getMatrices().translate(x + ((float) menuWidth / 2), y + ((float) menuHeight / 3), 0f);
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
}