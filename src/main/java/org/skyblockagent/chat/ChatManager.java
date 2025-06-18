package org.skyblockagent.chat;

import org.skyblockagent.gui.ChatScreen;

import java.util.UUID;

public class ChatManager {
    private final static ChatManager INSTANCE = new ChatManager();
    private Chat chat = new Chat();

    public void createChat() {
        chat = new Chat();
        ChatScreen.initDisplay(true, true);
    }

    public void loadChat(UUID id) {
        UUID currentChatId = chat.getId();
        if (currentChatId != null && currentChatId.equals(id)) return;
        chat = ChatStorage.getInstance().loadChat(id);

        ChatScreen.initDisplay(true, true);
    }

    public Chat getChat() {
        return chat;
    }

    public static ChatManager getInstance() {
        return INSTANCE;
    }
}
