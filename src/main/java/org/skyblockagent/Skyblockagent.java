package org.skyblockagent;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import org.skyblockagent.config.ConfigManager;
import org.skyblockagent.network.ApiAuth;
import org.skyblockagent.utils.CommandManager;
import org.skyblockagent.utils.Logger;
import org.skyblockagent.utils.Scheduler;

public class Skyblockagent implements ClientModInitializer {
    public static final String NAMESPACE = "skyblockagent";
    public static final String MOD_NAME = "Apex";
    public static final ModContainer MOD = FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow();
    public static final String VERSION = MOD.getMetadata().getVersion().getFriendlyString();
    public static final String MINECRAFT_VERSION = SharedConstants.getGameVersion().name();
    public static final Gson GSON = new Gson();

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ClientTickEvents.END_CLIENT_TICK.register(Scheduler.getInstance()::tick);
        Logger.init();
        CommandManager.init();
        ApiAuth.init();
    }
}