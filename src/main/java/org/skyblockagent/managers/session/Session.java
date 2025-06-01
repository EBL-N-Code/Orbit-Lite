package org.skyblockagent.managers.session;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.ChatManager;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private UUID chatId;
    private long createdAt;
    private long lastModified;
    private String title;
    private final InputState inputState = new InputState();
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    public Session() {}

    public Session(UUID chatId, long createdAt, long lastModified, String title, List<ChatMessage> chatMessages) {
        this.chatId = chatId;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.title = title;
        this.chatMessages.addAll(chatMessages);
    }

    public void init() {
        chatId = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        lastModified = createdAt;
        title = "Chat #" + ChatLoader.getChats().size();
    }

    public void send() {
        if (chatId == null) init();
        ChatManager chatManager = ChatManager.INSTANCE;

        UserMessage userMessage = new UserMessage(new ArrayList<>(inputState.getLines()));
        chatMessages.add(userMessage);

        inputState.reset();
        ChatScreen.INSTANCE.getInputBar().reset();
        lastModified = System.currentTimeMillis();

        AIMessage aiMessage = new AIMessage(new ArrayList<>(List.of("")));
        chatMessages.add(aiMessage);

        chatManager.sendRequest(aiMessage);
    }

    public InputState getInputState() {
        return inputState;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public UUID getId() {
        return chatId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getTitle() {
        return title;
    }
}
