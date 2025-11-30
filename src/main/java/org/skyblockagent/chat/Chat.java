package org.skyblockagent.chat;

import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.chat.message.ErrorMessage;
import org.skyblockagent.chat.message.UserMessage;
import org.skyblockagent.config.ConfigManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.inputBar.InputState;
import org.skyblockagent.network.BackendService;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String uuid;
    private String name;
    private long updatedAt;
    private final String type;
    private final InputState inputState = new InputState();
    private final List<ChatMessage> messages = new ArrayList<>();

    public Chat() {
        this.type = ConfigManager.getState().getChatMode().getName().toLowerCase();
    }

    public Chat(String uuid, String name, String type, long updatedAt, List<ChatMessage> messages) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
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
        boolean isErrorMsg = messages.getLast() instanceof ErrorMessage;
        removeLastMessage();
        String prompt = messages.reversed().stream()
                .filter(msg -> msg instanceof UserMessage)
                .findFirst()
                .map(ChatMessage::getContent)
                .orElse("");
        BackendService.sendMessage(this, prompt, !isErrorMsg);
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

    public String getType() {
        return type;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
