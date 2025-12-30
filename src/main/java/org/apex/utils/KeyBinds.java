package org.apex.utils;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.apex.Apex;
import org.apex.gui.ChatScreen;

public class KeyBinds {
    private static KeyBinding keyBinding;

    public static void init() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open " + Apex.MOD_NAME + " Chat",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));
    }

    public static void handleKeybinds() {
        while (keyBinding.wasPressed()) {
            ChatScreen.openScreen();
        }
    }
}
