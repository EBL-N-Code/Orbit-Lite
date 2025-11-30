package org.skyblockagent.chat;

public enum ChatMode {
    NORMAL("Normal"),
    SKYBLOCK("SkyBlock");

    private final String name;

    ChatMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}