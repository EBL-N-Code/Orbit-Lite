package org.skyblockagent;

import net.fabricmc.api.ClientModInitializer;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.network.AIClient;
import org.skyblockagent.network.Streamer;
import org.skyblockagent.utils.CommandManager;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";

    @Override
    public void onInitializeClient() {
        ChatStorage.getInstance().load();
        AIClient.initClient();
        Streamer.init();
        CommandManager.init();
    }
}