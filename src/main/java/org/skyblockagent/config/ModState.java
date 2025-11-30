package org.skyblockagent.config;

import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatMode;
import org.skyblockagent.gui.ChatScreen;

public class ModState {
    private ChatMode chatMode = ChatMode.SKYBLOCK;

    public ChatMode getChatMode() {
        return chatMode;
    }

    public void setChatMode(ChatMode chatMode) {
        ChatMode oldMode = this.chatMode;
        this.chatMode = chatMode;

        if (oldMode != chatMode) {
            ChatManager.getInstance().createChat();
            ChatScreen.updateSidebar();
        }
    }
}
