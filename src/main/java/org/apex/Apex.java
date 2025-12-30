package org.apex;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import org.apex.network.ApiAuth;
import org.apex.utils.CommandManager;
import org.apex.utils.KeyBinds;
import org.apex.utils.Logger;
import org.apex.utils.Scheduler;

public class Apex implements ClientModInitializer {
    public static final String NAMESPACE = "apex";
    public static final String MOD_NAME = "Apex";
    public static final ModContainer MOD = FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow();
    public static final String VERSION = MOD.getMetadata().getVersion().getFriendlyString();
    public static final String MINECRAFT_VERSION = SharedConstants.getGameVersion().name();
    public static final Gson GSON = new Gson();

    @Override
    public void onInitializeClient() {
//        ConfigManager.load();
        Logger.init();
        ClientTickEvents.END_CLIENT_TICK.register(Scheduler.getInstance()::tick);
        ApiAuth.init();
        CommandManager.init();
        KeyBinds.init();
    }
}