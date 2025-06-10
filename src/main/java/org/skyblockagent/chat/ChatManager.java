package org.skyblockagent.chat;

import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.network.AIRequest;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.message.AIMessage;

import java.util.List;
import java.util.UUID;

public class ChatManager {
    private final static ChatManager INSTANCE = new ChatManager();
    private Chat chat = new Chat();
    private AIRequest request;

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

    public void sendRequest(List<ChatMessage> content) {
        ChatScreen.getInstance().getInputBar().setActivation(false);
        request = new AIRequest(content);
    }

    public boolean hasActiveRequest() {
        return request != null && !request.isCompleted();
    }

    public Chat getChat() {
        return chat;
    }

    public static ChatManager getInstance() {
        return INSTANCE;
    }
}
