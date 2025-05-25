package org.skyblockagent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.skyblockagent.ai.AIClient;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.utils.scheduler.Scheduler;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";

    @Override
    public void onInitializeClient() {
        AIClient.initClient();
        registerCommand();
    }

    public void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommandManager.literal("chat")
                        .executes(Scheduler.openScreen(() -> createGUI(null)))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommandManager.literal("reset")
                        .executes((_) -> {
                            ChatManager.INSTANCE.createSession();
                            return 1;
                        })));
    }

    public static Screen createGUI(@Nullable Screen parent) {
        return new ChatScreen(parent);
    }
}