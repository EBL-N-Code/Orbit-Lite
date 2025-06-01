package org.skyblockagent.managers.session;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.gui.ChatList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.*;

public class ChatLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatLoader.class);
    private static final Path INDEX_PATH = FabricLoader.getInstance().getConfigDir().resolve(Skyblockagent.NAMESPACE, "chat_index.json");
    private static final Type TYPE = new TypeToken<List<Chat>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Chat> CHATS = new ArrayList<>();
    private static final List<Line> LINES = new ArrayList<>();
    private static int maxOffset;

    public static void load() {
        if (!Files.exists(INDEX_PATH)) return;

        try (BufferedReader reader = Files.newBufferedReader(INDEX_PATH)) {
            List<Chat> loaded = GSON.fromJson(reader, TYPE);
            if (loaded != null) CHATS.addAll(loaded);
        } catch (IOException e) {
            LOGGER.error("Failed to load chat index: {}", e.getMessage(), e);
        }

        buildChats();
    }

    public static void updateIndexFor(Session session) {
        UUID id = session.getId();
        boolean updated = false;
        boolean isNewest = false;

        for (Chat chat : CHATS) {
            if (chat.getId().equals(id)) {
                chat.update(session.getLastModified());
                isNewest = CHATS.indexOf(chat) == 0;
                updated = true;
                break;
            }
        }

        if (!updated) CHATS.add(new Chat(id, session.getTitle(), session.getLastModified()));
        if (!isNewest) {
            buildChats();
        }
        save();
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(INDEX_PATH)) {
            GSON.toJson(CHATS, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save chat index: {}", e.getMessage(), e);
        }
    }

    public static void buildChats() {
        CHATS.sort(Comparator.comparing(Chat::getLastModified).reversed());
        LINES.clear();

        Pair<Integer, List<Line>> pair = ChatGrouper.groupChats(CHATS);

        maxOffset = pair.getLeft();
        LINES.addAll(pair.getRight());
    }

    public static List<Chat> getChats() {
        return CHATS;
    }

    public static int getMaxOffset() {
        return maxOffset;
    }

    public static List<Line> getLines() {
        return LINES;
    }
}