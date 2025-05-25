package org.skyblockagent.managers;

import org.skyblockagent.ai.AIRequest;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.message.AIMessage;

public class ChatManager {
    public final static ChatManager INSTANCE = new ChatManager();
    private Session session = new Session();
    private AIRequest request;

    public void createSession() {
        session = new Session();
    }

    public void sendRequest(AIMessage aiMessage) {
        ChatScreen.INSTANCE.getInputBar().setActivation(false);
        this.request = new AIRequest(aiMessage, session.getChatMessages());
    }

    public boolean hasActiveRequest() {
        return request != null && !request.isCompleted();
    }

    public Session getSession() {
        return session;
    }
}
