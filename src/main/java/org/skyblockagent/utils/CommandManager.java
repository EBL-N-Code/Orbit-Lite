package org.skyblockagent.utils;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.gui.ChatScreen;

public class CommandManager {

    public static void init() {
        register("chat", ChatScreen::openScreen);
        register("reset", ChatManager.getInstance()::createChat);
        register("rebuildIndex", ChatStorage.getInstance()::rebuildIndex);
    }

    public static void register(String command, Runnable action) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommandManager.literal(command)
                        .executes((_) -> {
                            action.run();
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
