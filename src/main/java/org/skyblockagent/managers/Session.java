package org.skyblockagent.managers;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private UUID chatId;
    private final InputState inputState = new InputState();
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    public Session() {}

    public Session(UUID chatId) {
        this.chatId = chatId;
    }

    public void init() {
        chatId = UUID.randomUUID();
    }

    public void send() {
        if (chatId == null) init();

        UserMessage userMessage = new UserMessage(new ArrayList<>(inputState.getLines()));
        chatMessages.add(userMessage);

        inputState.reset();
        ChatScreen.INSTANCE.getInputBar().reset();

        AIMessage aiMessage = new AIMessage(new ArrayList<>(List.of("")));
        chatMessages.add(aiMessage);

        ChatManager.INSTANCE.sendRequest(aiMessage);
    }

    public InputState getInputState() {
        return inputState;
    }

    public boolean isEmpty() {
        return chatMessages.isEmpty();
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }
}
