package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;

public class ErrorMessage extends ChatMessage {

    @Override
    public void init(int x, int y, int width) {

    }

    @Override
    public void render(DrawContext context) {

    }

    @Override
    public void calculateWidth(int width) {

    }

    @Override
    public void calculateHeight() {

    }

    @Override
    public String getContent() {
        return "";
    }

    @Override
    public Sender getSender() {
        return Sender.AI;
    }
}
