package org.apex.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import org.apex.Constants;
import org.apex.chat.*;
import org.apex.chat.message.AIMessage;
import org.apex.utils.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.apex.Apex.GSON;

public class BackendService {
    private static Map<DateGroup, List<ChatSummary>> chatSummariesByDate = new HashMap<>();
    private static boolean hasFetchedChats;
    public static boolean activeCall;

    protected static void putChatFirst(Chat chat) {
        String chatUuid = chat.getUuid();

        // Remove from any existing list
        for (List<ChatSummary> chats : chatSummariesByDate.values()) {
            chats.removeIf(summary -> summary.uuid().equals(chatUuid));
        }

        // Add to TODAY
        ChatSummary chatSummary = ChatSummary.fromChat(chat);
        chatSummariesByDate.computeIfAbsent(DateGroup.TODAY, k -> new ArrayList<>()).addFirst(chatSummary);
    }

    protected static void fetchChats() {
        if (hasFetchedChats) return;
        try {
            HttpUtils.sendGetRequest(Constants.SERVER_IP + "/api/chats", ApiAuth.getToken())
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

                            List<ChatSummary> chats = GSON.fromJson(element.get("chats"), new TypeToken<List<ChatSummary>>(){}.getType());
                            chatSummariesByDate = ChatGrouper.groupChats(chats);

                            hasFetchedChats = true;
                        } catch (Exception e) {
                            Logger.error("Failed to fetch chats!", e);
                        }
                    }));
        } catch (Exception e) {
            Logger.error("Failed to fetch chats!", e);
        }
    }

    public static void sendMessage(Chat chat, String prompt, boolean retry) {
        activeCall = true;

        String chatUuid = chat.getUuid();
        boolean isNew = chatUuid == null;
        chatUuid = isNew ? "new" : chatUuid;

        JsonObject request = new JsonObject();
        request.addProperty("chat_uuid", chatUuid);
        request.addProperty("prompt", prompt);
        if (retry) request.addProperty("retry", true);

        try {
            AIMessage message = chat.createAIMessage();

            HttpUtils.sendStreamingRequest(Constants.SERVER_IP + "/api/chat/completions", GSON.toJson(request), ApiAuth.getToken(), event -> {
                switch (event) {
                    case HttpUtils.StreamEvent.Data data -> message.append(data.content());
                    case HttpUtils.StreamEvent.Info info -> {
                        JsonObject json = JsonParser.parseString(info.content()).getAsJsonObject();
                        if (isNew && json.has("chat_info")) {
                            JsonObject chatInfo = json.getAsJsonObject("chat_info");
                            String uuid = chatInfo.get("uuid").getAsString();
                            String name = chatInfo.get("name").getAsString();
                            chat.init(uuid, name);
                        }
                    }
                    case HttpUtils.StreamEvent.Error err -> {
                        activeCall = false;
                        chat.onError(err.error());
                        Logger.error(err.error(), err.throwable());
                    }
                    case HttpUtils.StreamEvent.Done data -> {
                        if (data.content() != null) {
                            JsonObject json = JsonParser.parseString(data.content()).getAsJsonObject();
                            if (json.has("usage")) {
                                JsonObject usage = json.getAsJsonObject("usage");
                                int inputTokens = usage.get("input_tokens").getAsInt();
                                int outputTokens = usage.get("output_tokens").getAsInt();
                                int cachedTokens = usage.get("cached_tokens").getAsInt();
                                int promptTokens = usage.get("prompt_tokens").getAsInt();
                                int tokensUsage = usage.get("tokens_usage").getAsInt();
                                int latency = usage.get("latency_ms").getAsInt();
                                int tps = latency > 0 ? (int) (outputTokens / (latency / 1000.0)) : 0;
                                message.setUsage(new AIMessage.UsageInfo(inputTokens, outputTokens,
                                        promptTokens, cachedTokens, tokensUsage, latency, tps));

                                chat.setUpdatedAt(System.currentTimeMillis());
                                putChatFirst(chat);

                                UserInfo userInfo = ApiAuth.getUserInfo();
                                if (userInfo != null) userInfo.addTokens(tokensUsage);
                            }
                        }
                        message.onFinish();
                        activeCall = false;
                    }
                }
            });
        } catch (Exception e) {
            Logger.error("Failed to send prompt!", e);
            activeCall = false;
        }
    }

    public static CompletableFuture<Chat> fetchChat(String uuid) {
        try {
            return HttpUtils.sendGetRequest(Constants.SERVER_IP + "/api/chat/" + uuid, ApiAuth.getToken())
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
            return HttpUtils.sendRequest(Constants.SERVER_IP + "/api/chat/" + uuid, "DELETE", null, ApiAuth.getToken())
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
        return chatSummariesByDate;
    }

    public static boolean hasActiveCall() {
        return activeCall;
    }
}
