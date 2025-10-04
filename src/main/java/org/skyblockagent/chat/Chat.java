package org.skyblockagent.chat;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.gui.inputBar.InputBar;
import org.skyblockagent.gui.inputBar.InputState;
import org.skyblockagent.chat.message.UserMessage;
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
        InputBar inputBar = ChatScreen.getInstance().getInputBar();
        String prompt = String.join("\n", inputState.getLines());
        messages.add(new UserMessage(new ArrayList<>(inputState.getLines())));
        inputState.clear();
        inputBar.reset();
        BackendService.sendMessage(this, prompt);
    }

    public void addAiMessage(AIMessage message) {
        messages.add(message);
    }

    public void retry() {
//        if (BackendService.hasActiveCall()) return;
//        ChatMessage lastMessage = messages.removeLast();
//        ChatScreen.getInstance().getChatDisplay().addSpace(-(lastMessage.getHeight() + 5));
//        addAiMessage();
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
