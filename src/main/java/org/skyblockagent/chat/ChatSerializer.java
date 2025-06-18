package org.skyblockagent.chat;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skyblockagent.message.AIMessage;
import org.skyblockagent.message.ChatMessage;
import org.skyblockagent.message.Sender;
import org.skyblockagent.message.UserMessage;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ChatSerializer {
    private static final Type CHAT_MSG_TYPE = new TypeToken<List<ChatMessage>>(){}.getType();
    private static final Gson FULL_CHAT_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Chat.class, new ChatAdapter())
            .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
            .create();

    private static final Gson INDEX_CHAT_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Chat.class, new ChatIndexAdapter())
            .create();

    public static Gson getFullChatGson() {
        return FULL_CHAT_GSON;
    }

    public static Gson getIndexChatGson() {
        return INDEX_CHAT_GSON;
    }

    private static class ChatAdapter implements JsonSerializer<Chat>, JsonDeserializer<Chat> {
        @Override
        public JsonElement serialize(Chat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("id", src.getId().toString());
            json.addProperty("title", src.getTitle());
            json.addProperty("created", src.getCreatedAt());
            json.addProperty("modified", src.getModifiedAt());
            List<ChatMessage> messages = src.getMessages().stream().filter(m -> !m.isError()).toList();
            json.add("messages", context.serialize(messages, CHAT_MSG_TYPE));
            return json;
        }

        @Override
        public Chat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID id = UUID.fromString(obj.get("id").getAsString());
            String title = obj.get("title").getAsString();
            long createdAt = obj.get("created").getAsLong();
            long modifiedAt = obj.get("modified").getAsLong();
            List<ChatMessage> messages = context.deserialize(obj.get("messages"), CHAT_MSG_TYPE);
            return new Chat(id, title, modifiedAt, createdAt, messages);
        }
    }

    private static class ChatIndexAdapter implements JsonSerializer<Chat>, JsonDeserializer<Chat> {
        @Override
        public JsonElement serialize(Chat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("id", src.getId().toString());
            json.addProperty("title", src.getTitle());
            json.addProperty("modified", src.getModifiedAt());
            return json;
        }

        @Override
        public Chat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID id = UUID.fromString(obj.get("id").getAsString());
            String title = obj.get("title").getAsString();
            long modifiedAt = obj.get("modified").getAsLong();
            return new Chat(id, title, modifiedAt);
        }
    }

    private static class ChatMessageAdapter implements JsonSerializer<ChatMessage>, JsonDeserializer<ChatMessage> {
        @Override
        public JsonElement serialize(ChatMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("role", src.getSender() == Sender.USER ? "user" : "ai");
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