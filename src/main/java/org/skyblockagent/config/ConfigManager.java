package org.skyblockagent.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.skyblockagent.Skyblockagent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModState STATE = new ModState();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                STATE = GSON.fromJson(json, ModState.class);
            } else {
                save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
            STATE = new ModState();
        }
    }

    public static void save() {
        try {
            String json = GSON.toJson(STATE);
            Files.writeString(FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public static ModState getState() {
        return STATE;
    }
}
