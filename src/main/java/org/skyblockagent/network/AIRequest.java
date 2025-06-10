package org.skyblockagent.network;

import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;

import java.util.List;

public class AIRequest {
    private final AIMessage aiMessage;
    private boolean completed = false;

    public AIRequest(List<ChatMessage> content) {
        aiMessage = (AIMessage) content.getLast();
        Streamer.start(aiMessage);
        AIClient.sendRequest(this, content);
    }

    public void onPartial(String partialChunk) {
        Streamer.queueChunk(partialChunk);
    }

    public void onFinish() {
        completed = true;
        Streamer.queueFinish();
    }

    public void onError(String error) {
        aiMessage.onError(error);
        onFinish();
    }

    public void setRunInfo(RunInfo runInfo) {
        aiMessage.setRunInfo(runInfo);
    }

    public boolean isCompleted() {
        return completed;
    }
}
