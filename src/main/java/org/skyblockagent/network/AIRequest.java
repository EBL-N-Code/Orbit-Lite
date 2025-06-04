package org.skyblockagent.network;

import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;

import java.util.List;

public class AIRequest {
    private final long timestamp = System.currentTimeMillis();
    private final AIMessage aiMessage;
    private String requestId;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double totalTime;

    private boolean completed = false;

    public AIRequest(AIMessage aiMessage, List<ChatMessage> messages) {
        this.aiMessage = aiMessage;

        StreamingProcessor.start(aiMessage);
        AIClient.sendRequest(this, messages);
    }

    public void onPartial(String partialChunk) {
        StreamingProcessor.queueChunk(partialChunk);
    }

    public void onFinish() {
        completed = true;
        StreamingProcessor.queueFinish();
    }

    public void onError(String error) {
        onPartial(error);
        onFinish();
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
