package org.skyblockagent.utils;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.skyblockagent.gui.ChatScreen;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static final Map<String, Long> cooldowns = new HashMap<>();

    public static void init() {
        register("chat", ChatScreen::openScreen, 500);
    }

    public static void register(String command, Runnable action, long cooldown) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, ignored) ->
                dispatcher.register(ClientCommandManager.literal(command)
                        .executes((ignored2) -> {
                            long now = System.currentTimeMillis();
                            long lastUsed = cooldowns.getOrDefault(command, 0L);

                            var player = MinecraftClient.getInstance().player;
                            if (now - lastUsed < cooldown && player != null) {
                                long left = (long) Math.ceil((cooldown - (now - lastUsed)) / 1000.0);
                                player.sendMessage(
                                        Text.literal("§cPlease wait " + left + "s before using that command again!"),
                                        false
                                );
                                return 0;
                            }

                            cooldowns.put(command, now);
                            action.run();
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
