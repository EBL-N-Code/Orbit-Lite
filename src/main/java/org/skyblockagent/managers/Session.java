package org.skyblockagent.managers;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private UUID chatId;
    private InputState inputState = new InputState();
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    public Session() {}

    public Session(UUID chatId) {
        this.chatId = chatId;
    }

    public void init() {
        chatId = UUID.randomUUID();
        ChatScreen.INSTANCE.initDisplay();
    }

    public void addMessage() {
        boolean firstMessage = isPreview();
        if (firstMessage) init();

        ChatScreen chatScreen = ChatScreen.INSTANCE;

        ChatMessage chatMessage = new UserMessage(new ArrayList<>(inputState.getLines()));
        chatScreen.getChatDisplay().initMessage(chatMessage);
        chatMessages.add(chatMessage);

        inputState.reset();
        ChatScreen.INSTANCE.onMessage(firstMessage);
    }

    public boolean isPreview() {
        return chatId == null;
    }

    public InputState getInputState() {
        return inputState;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }
}
