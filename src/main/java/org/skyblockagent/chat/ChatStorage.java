package org.skyblockagent.chat;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.skyblockagent.Skyblockagent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ChatStorage {
    private static final ChatStorage INSTANCE = new ChatStorage();
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatStorage.class);
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE);
    private static final Path INDEX_PATH = CONFIG_DIR.resolve("chat_index.json");
    private static final Path CHAT_FOLDER = CONFIG_DIR.resolve("chats");
    private static final Type TYPE = new TypeToken<List<Chat>>() {}.getType();
    private final List<Chat> CHATS = new ArrayList<>();
    private final Map<DateGroup, List<Chat>> GROUPED_CHATS = new EnumMap<>(DateGroup.class);
    private int maxOffset;

    public void load() {
        if (Files.exists(INDEX_PATH)) {
            initDirectories();
            loadIndex();
            buildChats();
        } else if (Files.exists(CHAT_FOLDER)) {
            rebuildIndex();
        } else {
            initDirectories();
        }
    }

    private void initDirectories() {
        try {
            Files.createDirectories(CHAT_FOLDER);
        } catch (IOException e) {
            LOGGER.error("Failed to create chat folder: {}", e.getMessage(), e);
        }
    }

    private void loadIndex() {
        try (BufferedReader reader = Files.newBufferedReader(INDEX_PATH)) {
            List<Chat> loaded = ChatSerializer.getIndexChatGson().fromJson(reader, TYPE);
            if (loaded != null) CHATS.addAll(loaded);
        } catch (IOException e) {
            LOGGER.error("Failed to load chat index: {}", e.getMessage(), e);
            LOGGER.info("Rebuilding chat index");
            rebuildIndex();
        }
    }

    private void saveIndex() {
        try (BufferedWriter writer = Files.newBufferedWriter(INDEX_PATH)) {
            ChatSerializer.getIndexChatGson().toJson(CHATS, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save chat index: {}", e.getMessage(), e);
        }
    }

    public void saveChat() {
        Chat chat = ChatManager.getInstance().getChat();
        Path filePath = CHAT_FOLDER.resolve(chat.getId().toString() + ".json");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            ChatSerializer.getFullChatGson().toJson(chat, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to serialize chat {}: {}", chat.getId(), e.getMessage(), e);
        }
        updateIndexFor(chat);
    }

    public Chat loadChat(UUID id) {
        Chat chat = null;
        for (Chat c : CHATS) {
            if (c.getId().equals(id)) {
                if (c.isLoaded()) return c;
                chat = c;
                break;
            }
        }

        Path filePath = CHAT_FOLDER.resolve(id.toString() + ".json");
        if (!Files.exists(filePath)) {
            LOGGER.error("Chat file not found: {}", filePath);
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            Chat loadedChat = ChatSerializer.getFullChatGson().fromJson(reader, Chat.class);
            if (chat != null) {
                chat.init(loadedChat.getCreatedAt(), loadedChat.getMessages());
                return chat;
            } else {
                CHATS.add(loadedChat);
                updateIndexFor(loadedChat);
                return loadedChat;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load chat {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    private void updateIndexFor(Chat chat) {
        if (!CHATS.isEmpty() && !CHATS.getFirst().equals(chat)) {
            if (CHATS.stream().noneMatch(c -> c.getId().equals(chat.getId()))) {
                CHATS.add(chat);
            }
            buildChats();
        }
        saveIndex();
    }

    private void buildChats() {
        GROUPED_CHATS.clear();
        Pair<Integer, Map<DateGroup, List<Chat>>> pair = ChatGrouper.groupChats(CHATS);
        maxOffset = pair.getLeft();
        GROUPED_CHATS.putAll(pair.getRight());
    }

    public void rebuildIndex() {
        CHATS.clear();
        try (var stream = Files.list(CHAT_FOLDER)) {
            stream.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                            if (obj.has("id") && obj.has("title") && obj.has("modified")) {
                                Chat chat = new Chat(
                                        UUID.fromString(obj.get("id").getAsString()),
                                        obj.get("title").getAsString(),
                                        obj.get("modified").getAsLong()
                                );
                                CHATS.add(chat);
                            } else {
                                LOGGER.warn("Skipping chat file with missing required fields: {}", path);
                            }
                        } catch (IOException | JsonParseException | IllegalArgumentException e) {
                            LOGGER.error("Failed to process chat file {}: {}", path, e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Failed to scan chat folder: {}", e.getMessage(), e);
        }

        if (!CHATS.isEmpty()) {
            buildChats();
            saveIndex();
        }
    }

    public List<Chat> getChats() {
        return CHATS;
    }

    public int getMaxOffset() {
        return maxOffset;
    }

    public Map<DateGroup, List<Chat>> getGroupedChats() {
        return GROUPED_CHATS;
    }

    public static ChatStorage getInstance() {
        return INSTANCE;
    }
}
