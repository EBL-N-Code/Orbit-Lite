package org.skyblockagent.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.UserMessage;

import java.lang.reflect.Type;
import java.util.List;

public class SessionSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
            .create();

    // Get the configured Gson instance
    public static Gson getGson() {
        return GSON;
    }

    // Type adapter for ChatMessage to handle UserMessage and AIMessage
    private static class ChatMessageAdapter implements JsonSerializer<ChatMessage>, JsonDeserializer<ChatMessage> {
        @Override
        public JsonElement serialize(ChatMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("type", src instanceof UserMessage ? "user" : "ai");
            json.add("rawContent", context.serialize(src.getRawContent()));
            json.addProperty("timestamp", src.getTimestamp());
            return json;
        }

        @Override
        public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String type = obj.get("type").getAsString();
            List<String> rawContent = context.deserialize(obj.get("rawContent"), new TypeToken<List<String>>(){}.getType());
            long timestamp = obj.get("timestamp").getAsLong();
            return switch (type) {
                case "user" -> new UserMessage(rawContent, timestamp);
                case "ai" -> new AIMessage(rawContent, timestamp);
                default -> throw new JsonParseException("Unknown message type: " + type);
            };
        }
    }
}