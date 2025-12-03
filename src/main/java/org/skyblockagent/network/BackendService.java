package org.skyblockagent.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.chat.*;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.config.ConfigManager;
import org.skyblockagent.utils.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.skyblockagent.Skyblockagent.GSON;

public class BackendService {
    protected static final String BACKEND_SERVICE_IP = "http://localhost:3000";
    private static Map<ChatMode, Map<DateGroup, List<ChatSummary>>> chatSummariesByType = new HashMap<>();
    private static boolean hasFetchedChats;
    private static boolean activeCall;

    protected static void putChatFirst(Chat chat) {
        String chatUuid = chat.getUuid();
        Map<DateGroup, List<ChatSummary>> chatSummaries = chatSummariesByType.get(ChatMode.valueOf(chat.getType().toUpperCase()));
        if (chatSummaries != null) {
            outer:
            for (List<ChatSummary> chatList : chatSummaries.values()) {
                Iterator<ChatSummary> it = chatList.iterator();
                while (it.hasNext()) {
                    if (it.next().uuid().equals(chatUuid)) {
                        it.remove();
                        break outer;
                    }
                }
            }
            var today = chatSummaries.get(DateGroup.TODAY);
            if (today != null) {
                today.addFirst(ChatSummary.fromChat(chat));
            }
        }
    }

    protected static void fetchChats() {
        if (hasFetchedChats) return;
        try {
            HttpUtils.sendGetRequest(BACKEND_SERVICE_IP + "/api/chats", ApiAuth.getToken())
                    .thenAccept(response -> MinecraftClient.getInstance().execute(() -> {
                        try {
                            if (response == null) throw new NullPointerException("Failed to get a response");
                            int statusCode = response.statusCode();
                            String body = response.body();
                            JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                            if (statusCode != 200) {
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                throw new RuntimeException("API returned " + statusCode + " code: " + error);
                            }

                            deserializeChatSummaries(element);
                            hasFetchedChats = true;
                        } catch (Exception e) {
                            Logger.error("Failed to fetch chats!", e);
                        }
                    }));
        } catch (Exception e) {
            Logger.error("Failed to fetch chats!", e);
        }
    }

    private static void deserializeChatSummaries(JsonObject json) {
        List<ChatSummary> chats = GSON.fromJson(json.get("chats"), new TypeToken<List<ChatSummary>>(){}.getType());

        chatSummariesByType = new HashMap<>();
        for (ChatMode mode : ChatMode.values()) {
            chatSummariesByType.put(mode, new HashMap<>());
        }

        Map<ChatMode, List<ChatSummary>> chatsByType = new HashMap<>();
        for (ChatSummary summary : chats) {
            try {
                chatsByType.computeIfAbsent(ChatMode.valueOf(summary.type().toUpperCase()), ignored -> new ArrayList<>()).add(summary);
            } catch (Exception e) {
                Logger.error("Failed to parse chat summary!", e);
            }
        }

        for (ChatMode mode : ChatMode.values()) {
            List<ChatSummary> modeChats = chatsByType.getOrDefault(mode, new ArrayList<>());
            chatSummariesByType.put(mode, ChatGrouper.groupChats(modeChats));
        }
    }

    public static void sendMessage(Chat chat, String prompt, boolean retry) {
        activeCall = true;

        String chatUuid = chat.getUuid();
        boolean isNew = chatUuid == null;
        chatUuid = isNew ? "new" : chatUuid;

        JsonObject request = new JsonObject();
        request.addProperty("chat_uuid", chatUuid);
        request.addProperty("chat_type", chat.getType());
        request.addProperty("prompt", prompt);
        if (retry) request.addProperty("retry", true);

        try {
            AIMessage message = chat.createAIMessage();

            HttpUtils.sendStreamingRequest(BACKEND_SERVICE_IP + "/api/chat/completions", GSON.toJson(request), ApiAuth.getToken(), event -> {
                switch (event) {
                    case HttpUtils.StreamEvent.Data data -> {
                        JsonObject json = JsonParser.parseString(data.json()).getAsJsonObject();
                        if (json.has("completions")) {
                            JsonObject completions = json.getAsJsonObject("completions");
                            if (completions.has("content")) {
                                String content = completions.get("content").getAsString();
                                message.append(content);
                            }
                        } else if (isNew && json.has("chat_info")) {
                            JsonObject chatInfo = json.getAsJsonObject("chat_info");
                            String uuid = chatInfo.get("uuid").getAsString();
                            String name = chatInfo.get("name").getAsString();
                            chat.init(uuid, name);
                        }
                    }
                    case HttpUtils.StreamEvent.Error err -> {
                        activeCall = false;
                        chat.onError(err.error());
                        Logger.error("Caught error while response streaming: " + err.error(), err.throwable());
                    }
                    case HttpUtils.StreamEvent.Done data -> {
                        if (data.json() != null) {
                            JsonObject json = JsonParser.parseString(data.json()).getAsJsonObject();
                            if (json.has("usage")) {
                                JsonObject usage = json.getAsJsonObject("usage");
                                int inputTokens = usage.get("input_tokens").getAsInt();
                                int outputTokens = usage.get("output_tokens").getAsInt();
                                int promptTokens = usage.get("prompt_tokens").getAsInt();
                                int cachedInputTokens = usage.get("cached_input_tokens").getAsInt();
                                int tokensUsage = usage.get("tokens_usage").getAsInt();
                                int latency = usage.get("latency_ms").getAsInt();
                                int tps = latency > 0 ? (int) (outputTokens / (latency / 1000.0)) : 0;
                                message.setUsage(new AIMessage.UsageInfo(inputTokens, outputTokens,
                                        promptTokens, cachedInputTokens, tokensUsage, latency, tps));

                                chat.setUpdatedAt(System.currentTimeMillis());
                                putChatFirst(chat);

                                UserInfo userInfo = ApiAuth.getUserInfo();
                                if (userInfo != null) userInfo.addTokens(tokensUsage);
                            }
                        }
                        message.onFinish();
                    }
                }
            });
        } catch (Exception e) {
            Logger.error("Failed to send prompt!", e);
        }

        activeCall = false;
    }

    public static CompletableFuture<Chat> fetchChat(String uuid) {
        try {
            return HttpUtils.sendGetRequest(BACKEND_SERVICE_IP + "/api/chat/" + uuid, ApiAuth.getToken())
                    .thenApply(response -> {
                        try {
                            if (response == null) throw new NullPointerException("Failed to get a response");
                            int statusCode = response.statusCode();
                            String body = response.body();
                            JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                            if (statusCode != 200) {
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                throw new RuntimeException("API returned " + statusCode + " code: " + error);
                            }

                            return ChatSerializer.getChatGson().fromJson(element, Chat.class);
                        } catch (Exception e) {
                            Logger.error("Failed to fetch chat!", e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            Logger.error("Failed to fetch chat!", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static CompletableFuture<Void> deleteChat(String uuid) {
        try {
            return HttpUtils.sendRequest(BACKEND_SERVICE_IP + "/api/chat/" + uuid, "DELETE", null, ApiAuth.getToken())
                    .thenApply(response -> {
                        try {
                            if (response == null) throw new NullPointerException("Failed to get a response");
                            int statusCode = response.statusCode();

                            if (statusCode != 200) {
                                JsonObject element = JsonParser.parseString(response.body()).getAsJsonObject();
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                throw new RuntimeException("API returned " + statusCode + " code: " + error);
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to delete chat!", e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            Logger.error("Failed to delete chat!", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static Map<DateGroup, List<ChatSummary>> getChatSummaries() {
        return chatSummariesByType.getOrDefault(ConfigManager.getState().getChatMode(), new HashMap<>());
    }

    public static boolean hasActiveCall() {
        return activeCall;
    }
}
