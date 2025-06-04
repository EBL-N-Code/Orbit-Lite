package org.skyblockagent.chat;

import org.skyblockagent.chat.session.SessionLoader;
import org.skyblockagent.network.AIRequest;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.session.Session;

import java.util.HashMap;
import java.util.UUID;

public class ChatManager {
    public final static ChatManager INSTANCE = new ChatManager();
    private final HashMap<UUID, Session> cachedSessions = new HashMap<>();
    private Session session = new Session();
    private AIRequest request;

    public void createSession() {
        addToCache(session);
        session = new Session();
        if (ChatScreen.INSTANCE != null) {
            ChatScreen.INSTANCE.initializeDisplay(true, true);
        }
    }

    public void loadSession(UUID id) {
        if (cachedSessions.containsKey(id)) {
            addToCache(session);
            session = cachedSessions.get(id);
        } else {
            Session loadedSession = SessionLoader.loadSession(id);
            if (loadedSession == null) return;
            addToCache(session);
            session = loadedSession;
        }

        if (ChatScreen.INSTANCE != null) {
            ChatScreen.INSTANCE.initializeDisplay(true, true);
        }
    }

    public void addToCache(Session session) {
        UUID id = session.getId();
        if (id != null) cachedSessions.put(id, session);
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
