package org.skyblockagent.chat;

import net.minecraft.client.MinecraftClient;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.network.BackendService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.skyblockagent.network.ApiAuth.logErr;

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
                        if (chat == null) {
                            logErr("[SkyBlockAI] Failed to load chat! (chat is null)");
                            return;
                        }
                        this.chat = chat;
                        ChatScreen.updateDisplay(true, true);
                    }));
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to load chat!", e);
        }
    }

    public void removeChat(String uuid) {
        try {
            BackendService.deleteChat(uuid)
                    .thenAccept(_ -> MinecraftClient.getInstance().execute(() -> {
                        CACHED_CHATS.remove(uuid);
                        BackendService.getChatSummaries().forEach((_, summaries) ->
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
            logErr("[SkyBlockAI] Failed to delete chat!", e);
        }
    }

    public Chat getChat() {
        return chat;
    }

    public static ChatManager getInstance() {
        return INSTANCE;
    }
}
