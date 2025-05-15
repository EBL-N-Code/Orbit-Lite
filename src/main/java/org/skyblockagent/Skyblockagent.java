package org.skyblockagent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.Session;
import org.skyblockagent.utils.scheduler.Scheduler;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";

    @Override
    public void onInitializeClient() {
        registerCommand();
    }

    public void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("chat")
                        .executes(Scheduler.openScreen(() -> createGUI(null)))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("reset")
                        .executes((_) -> {
                            ChatManager.INSTANCE.session = new Session();
                            return 1;
                        })));
    }

    public static Screen createGUI(@Nullable Screen parent) {
        return new ChatScreen(parent);
    }
}