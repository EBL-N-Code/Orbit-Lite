package org.skyblockagent.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import org.skyblockagent.chat.*;
import org.skyblockagent.chat.message.AIMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.skyblockagent.Skyblockagent.GSON;
import static org.skyblockagent.network.ApiAuth.logErr;

public class BackendService {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    protected static final String BACKEND_SERVICE_IP = "http://localhost:3000";
    private static Map<DateGroup, List<ChatSummary>> chatSummaries = new HashMap<>();
    private static boolean hasFetchedChats;
    private static boolean activeCall;

    protected static void putChatFirst(Chat chat) {
        String chatUuid = chat.getUuid();
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
                            chatSummaries = ChatGrouper.groupChats(chats);
                            hasFetchedChats = true;
                        } catch (Exception e) {
                            logErr("[SkyBlockAI] Failed to parse chats!", e);
                        }
                    }));
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to fetch chats!", e);
        }
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
            request.addProperty("prompt", prompt);
            if (retry) request.addProperty("retry", true);

            AIMessage aiMessage = chat.createAIMessage();

            HttpUtils.sendStreamingRequest(BACKEND_SERVICE_IP + "/api/chat/completions", GSON.toJson(request), ApiAuth.getToken(), event -> {
                switch (event) {
                    case HttpUtils.StreamEvent.Data data -> {
                        JsonObject json = JsonParser.parseString(data.json()).getAsJsonObject();
                        if (json.has("completions")) {
                            JsonObject completions = json.getAsJsonObject("completions");
                            if (completions.has("content")) {
                                String content = completions.get("content").getAsString();
                                aiMessage.append(content);
                            }
                        }
                        if (isNew && json.has("chat_info")) {
                            JsonObject chatInfo = json.getAsJsonObject("chat_info");
                            String uuid = chatInfo.get("uuid").getAsString();
                            String name = chatInfo.get("name").getAsString();
                            chat.init(uuid, name);
                        }
                        if (json.has("usage")) {
                            JsonObject usage = json.getAsJsonObject("usage");
                            int completionTokens = usage.get("completion_tokens").getAsInt();
                            int promptTokens = usage.get("prompt_tokens").getAsInt();
                            int totalTime = usage.get("total_time").getAsInt();
                            long updatedAt = usage.get("updated_at").getAsLong();
                            int usedTokensToday = usage.get("used_tokens_today").getAsInt();
                            UserInfo userInfo = ApiAuth.getUserInfo();
                            if (userInfo != null) userInfo.setUsedTokensToday(usedTokensToday);
                            aiMessage.setUsage(completionTokens, promptTokens, totalTime);
                            chat.setUpdatedAt(updatedAt);
                            putChatFirst(chat);
                        }
                    }
                    case HttpUtils.StreamEvent.Error err -> {
                        activeCall = false;
                        chat.onError(err.error());
                        ApiAuth.logStreamingErr(err.throwable());
                    }
                    case HttpUtils.StreamEvent.Done _ -> {
                        activeCall = false;
                        aiMessage.onFinish();
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
        return chatSummaries;
    }

    public static boolean hasActiveCall() {
        return activeCall;
    }
}
