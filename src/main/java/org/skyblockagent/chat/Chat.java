package org.skyblockagent.chat;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.message.AIMessage;
import org.skyblockagent.message.ChatMessage;
import org.skyblockagent.message.UserMessage;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.gui.inputBar.InputState;
import org.skyblockagent.network.Client;
import org.skyblockagent.network.Streamer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Chat {
    private UUID id;
    private String title;
    private long modifiedAt;
    private long createdAt;
    private final InputState inputState = new InputState();
    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean loaded;

    public Chat() {}

    public Chat(UUID id, String title, long modifiedAt) {
        this.id = id;
        this.title = title;
        this.modifiedAt = modifiedAt;
        this.loaded = false;
    }

    public Chat(UUID id, String title, long modifiedAt, long createdAt, List<ChatMessage> messages) {
        this.id = id;
        this.title = title;
        this.modifiedAt = modifiedAt;
        this.createdAt = createdAt;
        this.messages.addAll(messages);
        this.loaded = true;
    }

    public void init(long createdAt, List<ChatMessage> messages) {
        this.createdAt = createdAt;
        this.messages.addAll(messages);
        this.loaded = true;
    }

    public void init() {
        id = UUID.randomUUID();
        title = "Chat #" + ChatStorage.getInstance().getChats().size();
        createdAt = System.currentTimeMillis();
        modifiedAt = createdAt;
        loaded = true;
    }

    public void send() {
        if (id == null) init();
        InputBar inputBar = ChatScreen.getInstance().getInputBar();
        messages.add(new UserMessage(new ArrayList<>(inputState.getLines())));

        inputState.reset();
        inputBar.reset();
        
        addAiMessage();
    }

    public void addAiMessage() {
        messages.add(new AIMessage());
        modifiedAt = System.currentTimeMillis();
        Client.send(messages);
    }

    public void retry() {
        if (Streamer.isActive()) return;
        ChatMessage lastMessage = messages.removeLast();
        ChatScreen.getInstance().getChatDisplay().addSpace(-(lastMessage.getHeight() + 5));
        addAiMessage();
    }

    public InputState getInputState() {
        return inputState;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public UUID getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
