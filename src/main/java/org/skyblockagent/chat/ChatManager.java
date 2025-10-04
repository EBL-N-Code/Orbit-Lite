package org.skyblockagent.chat;

import net.minecraft.client.MinecraftClient;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.network.BackendService;

import java.util.HashMap;
import java.util.Map;

import static org.skyblockagent.network.ApiAuth.logErr;

public class ChatManager {
    private final static ChatManager INSTANCE = new ChatManager();
    private final static Map<String, Chat> CACHED_CHATS = new HashMap<>();
    private Chat chat = new Chat();

    public void addChat(Chat chat, boolean newest) {
        CACHED_CHATS.put(chat.getUuid(), chat);
        if (newest) {
            BackendService.getChatSummaries().get(DateGroup.TODAY).addFirst(ChatSummary.fromChat(chat));
            ChatScreen.getInstance().getSidebar().init();
        }
    }

    public void createChat() {
        chat = new Chat();
        ChatScreen.initDisplay(true, true);
    }

    public void loadChat(String uuid) {
        String currentChatId = chat.getUuid();
        if (currentChatId != null && currentChatId.equals(uuid)) return;
        if (CACHED_CHATS.containsKey(uuid)) {
            chat = CACHED_CHATS.get(uuid);
            ChatScreen.initDisplay(true, true);
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
                        ChatScreen.initDisplay(true, true);
                    }));
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to load chat!", e);
        }
    }

    public Chat getChat() {
        return chat;
    }

    public static ChatManager getInstance() {
        return INSTANCE;
    }
}
