package org.skyblockagent.managers.session;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.managers.ChatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SessionLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionLoader.class);
    private static final Path CHAT_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE, "chats");
    private static final Gson GSON = SessionSerializer.getGson();

    public static void saveSession() {
        MinecraftClient.getInstance().executeAsync(_ -> {
            CompletableFuture.runAsync(() -> {
                createDir();
                Session session = ChatManager.INSTANCE.getSession();
                saveSessionToFile(session);
                ChatLoader.updateIndexFor(session);
            });
        });
    }

    private static void createDir() {
        try {
            Files.createDirectories(CHAT_FOLDER);
        } catch (IOException e) {
            LOGGER.error("Failed to create chat folder: {}", e.getMessage(), e);
        }
    }

    private static void saveSessionToFile(Session session) {
        Path filePath = CHAT_FOLDER.resolve(session.getId().toString() + ".json");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(session, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to serialize session: {}", e.getMessage(), e);
        }
    }

    public static Session loadSession(UUID id) {
        Path filePath = CHAT_FOLDER.resolve(id.toString() + ".json");
        if (!Files.exists(filePath)) {
            LOGGER.error("Session file not found: {}", filePath);
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return GSON.fromJson(reader, Session.class);
        } catch (IOException e) {
            LOGGER.error("Failed to load session: {}", e.getMessage(), e);
            return null;
        }
    }
}