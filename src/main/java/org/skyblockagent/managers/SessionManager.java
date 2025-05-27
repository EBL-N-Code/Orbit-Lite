package org.skyblockagent.managers;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class SessionManager {
    private static final Path CHAT_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE, "chats");
    private static final Gson GSON = SessionSerializer.getGson();

    /**
     * Saves a Session to a JSON file in the config directory with field order preserved.
     *
     * @param session The Session to save.
     * @throws IOException If the file cannot be written or serialization fails.
     */
    public static void saveSession(Session session) {
        // Ensure config directory exists
        try {
            Files.createDirectories(CHAT_FOLDER);
            // Create file path based on id
            Path filePath = CHAT_FOLDER.resolve(session.getId().toString() + ".json");

            // Serialize session to JSON
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                GSON.toJson(session, writer);
            } catch (JsonIOException e) {
                throw new IOException("Failed to serialize session: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a Session from a JSON file in the config directory.
     *
     * @param id The UUID of the session to load.
     * @return The loaded Session.
     * @throws IOException If the file does not exist or deserialization fails.
     */
    public static Session loadSession(UUID id) throws IOException {
        Path filePath = CHAT_FOLDER.resolve(id.toString() + ".json");
        File file = filePath.toFile();
        if (!file.exists()) {
            throw new IOException("Session file not found: " + filePath);
        }

        // Read and deserialize JSON from file
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, Session.class);
        } catch (JsonParseException e) {
            throw new IOException("Failed to deserialize session: " + e.getMessage(), e);
        }
    }
}
