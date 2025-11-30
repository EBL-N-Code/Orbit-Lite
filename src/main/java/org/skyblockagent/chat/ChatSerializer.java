package org.skyblockagent.chat;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.chat.message.ChatMessage;
import org.skyblockagent.chat.message.Sender;
import org.skyblockagent.chat.message.UserMessage;

import java.lang.reflect.Type;
import java.util.List;

public class ChatSerializer {
    private static final Type CHAT_MSG_TYPE = new TypeToken<List<ChatMessage>>(){}.getType();
    private static final Gson CHAT_GSON = new GsonBuilder()
            .registerTypeAdapter(Chat.class, new ChatAdapter())
            .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
            .create();

    public static Gson getChatGson() {
        return CHAT_GSON;
    }

    private static class ChatAdapter implements JsonDeserializer<Chat> {
        @Override
        public Chat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String uuid = obj.get("uuid").getAsString();
            String name = obj.get("name").getAsString();
            String type = obj.get("chat_type").getAsString();
            long updatedAt = obj.get("updated_at").getAsLong();
            List<ChatMessage> messages = context.deserialize(obj.get("messages"), CHAT_MSG_TYPE);
            return new Chat(uuid, name, type, updatedAt, messages);
        }
    }

    private static class ChatMessageAdapter implements JsonDeserializer<ChatMessage> {
        @Override
        public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Sender sender = Sender.valueOf(obj.get("sender").getAsString().toUpperCase());
            String content = obj.get("content").getAsString();
            return switch (sender) {
                case Sender.USER -> new UserMessage(content);
                case Sender.ASSISTANT -> new AIMessage(content);
            };
        }
    }
}