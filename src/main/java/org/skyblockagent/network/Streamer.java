package org.skyblockagent.network;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.gui.ChatScreen;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Streamer {
    private static final ConcurrentLinkedQueue<StreamEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private static AIMessage currentMessage = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Streamer::tick);
    }

    public static void start(AIMessage message) {
        currentMessage = message;
        eventQueue.clear();
    }

    public static void queueChunk(String chunk) {
        eventQueue.add(new StreamEvent.Chunk(chunk));
    }

    public static void queueFinish() {
        eventQueue.add(new StreamEvent.Finish());
    }

    public static void tick(MinecraftClient client) {
        while (!eventQueue.isEmpty()) {
            StreamEvent event = eventQueue.poll();
            if (event instanceof StreamEvent.Chunk(String text)) {
                currentMessage.add(text);
            } else if (event instanceof StreamEvent.Finish) {
                currentMessage.onFinish();
                ChatStorage.getInstance().saveChat();
                ChatScreen chatScreen = ChatScreen.getInstance();
                if (chatScreen != null) chatScreen.getInputBar().setActivation(true);
            }
        }
    }
}