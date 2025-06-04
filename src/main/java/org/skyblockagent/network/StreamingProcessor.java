package org.skyblockagent.network;

import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.session.SessionLoader;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StreamingProcessor {
    private static final ConcurrentLinkedQueue<StreamEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private static AIMessage currentMessage = null;
    private static boolean finished = false;

    public static void start(AIMessage message) {
        currentMessage = message;
        finished = false;
        eventQueue.clear();
    }

    public static void queueChunk(String chunk) {
        eventQueue.add(new StreamEvent.Chunk(chunk));
    }

    public static void queueFinish() {
        eventQueue.add(new StreamEvent.Finish());
    }

    public static void tick() {
        while (!eventQueue.isEmpty()) {
            StreamEvent event = eventQueue.poll();
            if (event instanceof StreamEvent.Chunk(String text)) {
                if (currentMessage != null) {
                    currentMessage.add(text);
                }
            } else if (event instanceof StreamEvent.Finish) {
                if (finished) continue;
                finished = true;

                SessionLoader.saveSession();
                ChatScreen.INSTANCE.getInputBar().setActivation(true);

                currentMessage = null;
            }
        }
    }
}