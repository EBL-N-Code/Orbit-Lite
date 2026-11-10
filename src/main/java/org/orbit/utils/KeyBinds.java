package org.orbit.utils;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.orbit.Orbit;
import org.orbit.gui.ChatScreen;

public class KeyBinds {
    private static KeyBinding keyBinding;

    public static void init() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open " + Orbit.MOD_NAME + " Chat",
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
