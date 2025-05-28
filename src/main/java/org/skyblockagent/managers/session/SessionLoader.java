package org.skyblockagent.managers.session;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.managers.ChatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class SessionLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionLoader.class);
    private static final Path CHAT_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE, "chats");
    private static final Gson GSON = SessionSerializer.getGson();

    private static void createDir() {
        try {
            Files.createDirectories(CHAT_FOLDER);
        } catch (IOException e) {
            LOGGER.error("Couldn't create dir");
        }
    }

    public static void saveSession() {
        long s = System.currentTimeMillis();
        Session session = ChatManager.INSTANCE.getSession();

        createDir();
        Path filePath = CHAT_FOLDER.resolve(session.getId().toString() + ".json");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(session, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to serialize session: {}", e.getMessage(), e);
        }
        System.out.println("Saving Took " + (System.currentTimeMillis() - s) + "ms");
    }

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