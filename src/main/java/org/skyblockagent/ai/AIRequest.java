package org.skyblockagent.ai;

import net.minecraft.client.MinecraftClient;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;

import java.util.List;

public class AIRequest {
    private final long timestamp = System.currentTimeMillis();
    private final AIMessage aiMessage;
    private final List<ChatMessage> messages;
    private String requestId;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double totalTime;
    private boolean completed = false;
    private String errorMessage = null;

    public AIRequest(AIMessage aiMessage, List<ChatMessage> messages) {
        this.aiMessage = aiMessage;
        this.messages = messages;
        send();
    }

    public void send() {
        AIClient.sendRequest(this, messages);
    }

    public void onPartial(String partialChunk) {
        MinecraftClient.getInstance().executeSync(() -> aiMessage.add(partialChunk));
    }

    public void onFinish() {
        completed = true;
        aiMessage.onFinish();
        ChatScreen.INSTANCE.getInputBar().setActivation(true);
    }

    public void onError(String error) {
        completed = true;
        aiMessage.add(error);
        errorMessage = error;
        ChatScreen.INSTANCE.getInputBar().setActivation(true);
    }

    public void setUsage(String id, int prompt, int completion, int total, double totalTime) {
        this.requestId = id;
        this.promptTokens = prompt;
        this.completionTokens = completion;
        this.totalTokens = total;
        this.totalTime = totalTime;
    }

    public boolean isCompleted() {
        return completed;
    }
}
