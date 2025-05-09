package org.skyblockagent.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SafeScreen extends Screen {
    protected SafeScreen(MutableText empty) {
        super(empty);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        safe(() -> super.render(context, mouseX, mouseY, delta));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return safeBooleanReturn(() -> super.keyPressed(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return safeBooleanReturn(() -> super.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return safeBooleanReturn(() -> super.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return safeBooleanReturn(() -> super.charTyped(chr, modifiers));
    }


    @Override
    public void tick() {
        // Any periodic logic you want to run
        safe(super::tick);
    }

    @Override
    public void close() {
        super.close();
        // Optional: clean-up logic here
    }

    // ---------- Error Wrappers ----------

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            handleScreenError(e);
        }
    }

    private boolean safeBooleanReturn(BooleanSupplierWithException supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            handleScreenError(e);
            return false;
        }
    }

    @FunctionalInterface
    interface BooleanSupplierWithException {
        boolean get() throws Exception;
    }

    private void handleScreenError(Exception e) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c[Query Bar] Error occurred, " + e.getMessage()), false);
        }
        e.printStackTrace();
        client.setScreen(null);
    }
}
