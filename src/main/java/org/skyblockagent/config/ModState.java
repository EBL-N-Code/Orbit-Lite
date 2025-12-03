package org.skyblockagent.config;

import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatMode;
import org.skyblockagent.gui.ChatScreen;

public class ModState {
    private ChatMode chatMode = ChatMode.SKYBLOCK;
    private static boolean disabled = true;
    private static boolean underMaintenance = false;

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

    public static void disableMod() {
        disabled = true;
    }

    public static void enableMod() {
        disabled = false;
    }

    public static boolean isDisabled() {
        return disabled;
    }

    public static boolean isUnderMaintenance() {
        return underMaintenance;
    }

    public static void setUnderMaintenance(boolean underMaintenance) {
        ModState.underMaintenance = underMaintenance;
    }
}
