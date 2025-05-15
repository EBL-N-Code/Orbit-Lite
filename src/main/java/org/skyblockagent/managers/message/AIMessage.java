package org.skyblockagent.managers.message;

import java.util.List;

public class AIMessage extends ChatMessage{
    private final long tokensUsed;
    private final long generationTimeMs;

    public AIMessage(List<String> content, long tokensUsed, long generationTimeMs) {
        super(content);
        this.tokensUsed = tokensUsed;
        this.generationTimeMs = generationTimeMs;
    }

    @Override
    public MessageSender getSender() {
        return MessageSender.AI;
    }

    public long getTokensUsed() { return tokensUsed; }
    public long getGenerationTimeMs() { return generationTimeMs; }
}
