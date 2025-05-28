package org.skyblockagent.managers.session;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skyblockagent.managers.message.AIMessage;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.MessageSender;
import org.skyblockagent.managers.message.UserMessage;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class SessionSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Session.class, new SessionAdapter())
            .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
            .create();

    public static Gson getGson() {
        return GSON;
    }

    private static class SessionAdapter implements JsonSerializer<Session>, JsonDeserializer<Session> {
        @Override
        public JsonElement serialize(Session src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("id", src.getId().toString());
            json.addProperty("title", src.getTitle());
            json.addProperty("created", src.getCreatedAt());
            json.addProperty("modified", src.getLastModified());
            json.add("messages", context.serialize(src.getChatMessages(), new TypeToken<List<ChatMessage>>(){}.getType()));
            return json;
        }

        @Override
        public Session deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID id = UUID.fromString(obj.get("id").getAsString());
            String title = obj.get("title").getAsString();
            long createdAt = obj.get("created").getAsLong();
            long lastModified = obj.get("modified").getAsLong();
            List<ChatMessage> messages = context.deserialize(obj.get("messages"), new TypeToken<List<ChatMessage>>(){}.getType());
            return new Session(id, createdAt, lastModified, title, messages);
        }
    }

    private static class ChatMessageAdapter implements JsonSerializer<ChatMessage>, JsonDeserializer<ChatMessage> {
        @Override
        public JsonElement serialize(ChatMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("role", src.getSender() == MessageSender.USER ? "user" : "ai");
            json.addProperty("content", src.getContent());
            json.addProperty("timestamp", src.getTimestamp());
            return json;
        }

        @Override
        public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String role = obj.get("role").getAsString();
            String content = obj.get("content").getAsString();
            long timestamp = obj.get("timestamp").getAsLong();
            return switch (role) {
                case "user" -> new UserMessage(content, timestamp);
                case "ai" -> new AIMessage(content, timestamp);
                default -> throw new JsonParseException("Unknown message role: " + role);
            };
        }
    }
}