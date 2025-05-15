package org.skyblockagent.managers.message;

import org.skyblockagent.managers.ChatManager;

import java.util.List;

public class UserMessage extends ChatMessage {
    public UserMessage(List<String> content) {
        super(content);
    }

    @Override
    public MessageSender getSender() {
        return MessageSender.USER;
    }
}
