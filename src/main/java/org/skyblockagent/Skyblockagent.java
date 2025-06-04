package org.skyblockagent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.skyblockagent.chat.session.ChatLoader;
import org.skyblockagent.network.AIClient;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.network.StreamingProcessor;
import org.skyblockagent.utils.scheduler.Scheduler;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";

    @Override
    public void onInitializeClient() {
        AIClient.initClient();
        registerCommand();
        ChatLoader.load();

        ClientTickEvents.END_CLIENT_TICK.register(_ -> StreamingProcessor.tick());
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