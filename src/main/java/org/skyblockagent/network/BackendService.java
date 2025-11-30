package org.skyblockagent.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.chat.*;
import org.skyblockagent.chat.message.AIMessage;
import org.skyblockagent.config.ConfigManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.skyblockagent.Skyblockagent.GSON;
import static org.skyblockagent.network.ApiAuth.logErr;

public class BackendService {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    protected static final String BACKEND_SERVICE_IP = "http://localhost:3000";
    private static Map<ChatMode, Map<DateGroup, List<ChatSummary>>> chatSummariesByType = new HashMap<>();
    private static boolean hasFetchedChats;
    private static boolean activeCall;

    protected static void putChatFirst(Chat chat) {
        String chatUuid = chat.getUuid();
        Map<DateGroup, List<ChatSummary>> chatSummaries = chatSummariesByType.get(ChatMode.valueOf(chat.getType().toUpperCase()));
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
        chatSummaries.get(DateGroup.TODAY).addFirst(ChatSummary.fromChat(chat));
    }

    protected static void fetchChats() {
        try {
            HttpUtils.sendGetRequest(BACKEND_SERVICE_IP + "/api/chats", ApiAuth.getToken())
                    .thenAccept(response -> CLIENT.execute(() -> {
                        try {
                            if (response == null) return;
                            int statusCode = response.statusCode();
                            String body = response.body();
                            JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                            if (statusCode != 200) {
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                logErr("[SkyBlockAI] Chats fetch returned " + statusCode + ": " + error);
                                return;
                            }

                            List<ChatSummary> chats = GSON.fromJson(element.get("chats"), new TypeToken<List<ChatSummary>>(){}.getType());
                            processChatSummaries(chats);
                        } catch (Exception e) {
                            logErr("[SkyBlockAI] Failed to parse chats!", e);
                        }
                    }));
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to fetch chats!", e);
        }
    }

    private static void processChatSummaries(List<ChatSummary> chats) {
        chatSummariesByType = new HashMap<>();
        for (ChatMode mode : ChatMode.values()) {
            chatSummariesByType.put(mode, new HashMap<>());
        }

        Map<ChatMode, List<ChatSummary>> chatsByType = new HashMap<>();
        for (ChatSummary summary : chats) {
            try {
                chatsByType.computeIfAbsent(ChatMode.valueOf(summary.type().toUpperCase()), ignored -> new ArrayList<>()).add(summary);
            } catch (IllegalArgumentException error) {
                System.out.println("Unknown chat type " + error);
            }
        }

        for (ChatMode mode : ChatMode.values()) {
            List<ChatSummary> modeChats = chatsByType.getOrDefault(mode, new ArrayList<>());
            chatSummariesByType.put(mode, ChatGrouper.groupChats(modeChats));
        }

        hasFetchedChats = true;
    }

    public static void sendMessage(Chat chat, String prompt, boolean retry) {
        if (prompt.isEmpty()) return;
        try {
            activeCall = true;
            String chatUuid = chat.getUuid();
            boolean isNew = chatUuid == null;
            chatUuid = isNew ? "new" : chatUuid;
            JsonObject request = new JsonObject();
            request.addProperty("chat_uuid", chatUuid);
            request.addProperty("chat_type", chat.getType());
            request.addProperty("prompt", prompt);
            if (retry) request.addProperty("retry", true);

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
                        ApiAuth.logStreamingErr(err.throwable());
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
                        activeCall = false;
                        message.onFinish();
                    }
                }
            });
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to send message!", e);
            activeCall = false;
        }
    }

    public static CompletableFuture<Chat> fetchChat(String uuid) {
        try {
            return HttpUtils.sendGetRequest(BACKEND_SERVICE_IP + "/api/chat/" + uuid, ApiAuth.getToken())
                    .thenApply(response -> {
                        try {
                            if (response == null) return null;
                            int statusCode = response.statusCode();
                            String body = response.body();
                            JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                            if (statusCode != 200) {
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                logErr("[SkyBlockAI] Chat fetch returned " + statusCode + ": " + error);
                                return null;
                            }

                            return ChatSerializer.getChatGson().fromJson(element, Chat.class);
                        } catch (Exception e) {
                            logErr("[SkyBlockAI] Failed to parse chat!", e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to fetch chat!", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static CompletableFuture<Void> deleteChat(String uuid) {
        try {
            return HttpUtils.sendRequest(BACKEND_SERVICE_IP + "/api/chat/" + uuid, "DELETE", null, ApiAuth.getToken())
                    .thenApply(response -> {
                        try {
                            if (response == null) return null;
                            int statusCode = response.statusCode();

                            if (statusCode != 200) {
                                JsonObject element = JsonParser.parseString(response.body()).getAsJsonObject();
                                String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                logErr("[SkyBlockAI] Chat fetch returned " + statusCode + ": " + error);
                            }
                        } catch (Exception e) {
                            logErr("[SkyBlockAI] Failed to delete chat!", e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to delete chat!", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static boolean hasFetchedChats() {
        return hasFetchedChats;
    }

    public static Map<DateGroup, List<ChatSummary>> getChatSummaries() {
        return chatSummariesByType.getOrDefault(ConfigManager.getState().getChatMode(), new HashMap<>());
    }

    public static boolean hasActiveCall() {
        return activeCall;
    }
}
