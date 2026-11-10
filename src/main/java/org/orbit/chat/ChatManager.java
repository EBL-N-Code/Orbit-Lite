package org.orbit.chat;

import net.minecraft.client.MinecraftClient;
import org.orbit.gui.ChatScreen;
import org.orbit.network.BackendService;
import org.orbit.utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatManager {
    private final static ChatManager INSTANCE = new ChatManager();
    private final static Map<String, Chat> CACHED_CHATS = new HashMap<>();
    private Chat chat = new Chat();

    public void addChat(Chat chat, boolean newChat) {
        CACHED_CHATS.put(chat.getUuid(), chat);
        if (newChat) {
            List<ChatSummary> todayChats = BackendService.getChatSummaries().get(DateGroup.TODAY);
            todayChats.addFirst(ChatSummary.fromChat(chat));
            ChatScreen.updateSidebar();
        }
    }

    public void createChat() {
        chat = new Chat();
        ChatScreen.updateDisplay(true, true);
    }

    public void loadChat(String uuid) {
        String currentChatId = chat.getUuid();
        if (currentChatId != null && currentChatId.equals(uuid)) return;
        if (CACHED_CHATS.containsKey(uuid)) {
            chat = CACHED_CHATS.get(uuid);
            ChatScreen.updateDisplay(true, true);
            return;
        }

        try {
            BackendService.fetchChat(uuid)
                    .thenAccept(chat -> MinecraftClient.getInstance().execute(() -> {
                        this.chat = Objects.requireNonNull(chat);
                        ChatScreen.updateDisplay(true, true);
                    }));
        } catch (Exception e) {
            Logger.error("Failed to load chat!", e);
        }
    }

    public void removeChat(String uuid) {
        try {
            BackendService.deleteChat(uuid)
                    .thenAccept(ignored -> MinecraftClient.getInstance().execute(() -> {
                        CACHED_CHATS.remove(uuid);
                        BackendService.getChatSummaries().forEach((ignored2, summaries) ->
                                summaries.removeIf(summary -> summary.uuid().equals(uuid))
                        );
                        if (chat != null) {
                            String currentChatUuid = chat.getUuid();
                            if (currentChatUuid != null && currentChatUuid.equals(uuid)) {
                                createChat();
                            }
                        }
                        ChatScreen.updateSidebar();
                    }));
        } catch (Exception e) {
            Logger.error("Failed to delete chat!", e);
        }
    }

    public Chat getChat() {
        return chat;
    }

    public static ChatManager getInstance() {
        return INSTANCE;
    }
}
