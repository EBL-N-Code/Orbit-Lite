package org.skyblockagent;

import net.fabricmc.api.ClientModInitializer;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.network.Client;
import org.skyblockagent.network.Streamer;
import org.skyblockagent.testing.Tooltip;
import org.skyblockagent.testing.ValueHud;
import org.skyblockagent.utils.CommandManager;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";

    @Override
    public void onInitializeClient() {
        ChatStorage.getInstance().load();
        Client.initClient();
        Streamer.init();
        CommandManager.init();
        Tooltip.registerTooltipCallback();
        ValueHud.register();
    }
}