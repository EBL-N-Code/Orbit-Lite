package org.skyblockagent.network;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.chat.Chat;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatStorage;
import org.skyblockagent.message.AIMessage;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Streamer {
    private static final int MESSAGE_PER_TICK = 4;
    private static final ConcurrentLinkedQueue<StreamEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private static AIMessage currentMessage = null;
    private static Chat currentChat = null;
    private static boolean active = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Streamer::tick);
    }

    public static void start(AIMessage message) {
        currentChat = ChatManager.getInstance().getChat();
        currentMessage = message;
        eventQueue.clear();
        active = true;
    }

    public static void queueChunk(String chunk) {
        eventQueue.add(new StreamEvent.Chunk(chunk));
    }

    public static void queueFinish() {
        eventQueue.add(new StreamEvent.Finish());
    }

    public static void onError(String error) {
        currentMessage.onError(error);
        queueFinish();
    }

    public static void interrupt() {
        eventQueue.clear();
        queueFinish();
        System.out.println("e");
    }

    public static void tick(MinecraftClient client) {
        int i = 0;
        while (!eventQueue.isEmpty() && i < MESSAGE_PER_TICK) {
            StreamEvent event = eventQueue.poll();
            if (event instanceof StreamEvent.Chunk(String text)) {
                currentMessage.append(text);
            } else if (event instanceof StreamEvent.Finish) {
                active = false;
                currentMessage.onFinish();
                ChatStorage.getInstance().saveChat(currentChat);
            }
            i++;
        }
    }

    public static void setUsage(String id, int promptTokens, int completionTokens, int totalTokens, double totalTime, double tps) {
        currentMessage.setRunInfo(new RunInfo(id, promptTokens, completionTokens, totalTokens, totalTime, tps));
    }

    public static boolean isActive() {
        return active;
    }
}