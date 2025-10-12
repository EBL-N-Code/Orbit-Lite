package org.skyblockagent.chat;

import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.chat.message.ErrorMessage;
import org.skyblockagent.chat.message.UserMessage;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.inputBar.InputState;
import org.skyblockagent.network.BackendService;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String uuid;
    private String name;
    private long updatedAt;
    private final InputState inputState = new InputState();
    private final List<ChatMessage> messages = new ArrayList<>();

    public Chat() {}

    public Chat(String uuid, String name, long updatedAt, List<ChatMessage> messages) {
        this.uuid = uuid;
        this.name = name;
        this.updatedAt = updatedAt;
        this.messages.addAll(messages);
        ChatManager.getInstance().addChat(this, false);
    }

    public void init(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        ChatManager.getInstance().addChat(this, true);
    }

    public void send() {
        String prompt = String.join("\n", inputState.getLines());
        createUserMessage();
        inputState.clear();
        BackendService.sendMessage(this, prompt, false);
    }

    public void retry() {
        if (BackendService.hasActiveCall()) return;
        removeLastMessage();
        String prompt = messages.reversed().stream()
                .filter(msg -> msg instanceof UserMessage)
                .findFirst()
                .map(ChatMessage::getContent)
                .orElse("");
        BackendService.sendMessage(this, prompt, true);
    }

    public void createUserMessage() {
        List<String> content = new ArrayList<>(inputState.getLines());
        UserMessage message = new UserMessage(content);
        addMessage(message);
    }

    public AIMessage createAIMessage() {
        AIMessage message = new AIMessage();
        addMessage(message);
        return message;
    }

    public void onError(String error) {
        removeLastMessage();
        addMessage(new ErrorMessage(error));
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) chatScreen.getChatDisplay().onMessage(message);
    }

    public void removeLastMessage() {
        messages.removeLast();
        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) chatScreen.getChatDisplay().calculateOffset();
    }

    public InputState getInputState() {
        return inputState;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public String getUuid() {
        return uuid;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
