package org.skyblockagent.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import okhttp3.*;
import org.skyblockagent.message.AIMessage;
import org.skyblockagent.message.ChatMessage;
import org.skyblockagent.message.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL = "meta-llama/llama-4-maverick-17b-128e-instruct";
    private static final Gson GSON = new Gson();
    private static OkHttpClient client;
    private static Call activeCall = null;
    private static volatile boolean interrupt = false;

    public static void initClient() {
        client = new OkHttpClient();
    }

    public static void send(List<ChatMessage> content) {
        interrupt = false;
        Streamer.start((AIMessage) content.getLast());
        MinecraftClient.getInstance().executeAsync(_ -> CompletableFuture.runAsync(() -> {
            try {
                JsonObject payload = buildPayload(content);
                Request httpRequest = buildHttpRequest(payload);
                activeCall = client.newCall(httpRequest);
                processResponse(activeCall);
            } catch (Exception e) {
                if (interrupt) return;
                LOGGER.error("API request error: {}", e.getMessage(), e);
                Streamer.onError(e.getMessage());
            } finally {
                activeCall = null;
            }
        }));
    }

    private static JsonObject buildPayload(List<ChatMessage> content) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        for (ChatMessage message : content) {
            if (message.isError()) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("role", message.getSender() == Sender.AI ? "assistant" : "user");
            obj.addProperty("content", message.getContent());
            messages.add(obj);
        }
        payload.add("messages", messages);

        return payload;
    }

    private static Request buildHttpRequest(JsonObject payload) {
        RequestBody body = RequestBody.create(GSON.toJson(payload), MediaType.get("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private static void processResponse(Call call) throws IOException {
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) {
                String errorMessage = "API request failed with code: " + response.code();
                LOGGER.error(errorMessage);
                Streamer.onError(errorMessage);
                return;
            }

            if (response.body() == null) {
                String errorMessage = "No response body";
                LOGGER.error(errorMessage);
                Streamer.onError(errorMessage);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        if (!line.isBlank()) LOGGER.info("Unknown line: {}", line);
                        continue;
                    }
                    line = line.substring("data:".length()).trim();
                    if (line.equals("[DONE]")) break;
                    processChunk(line);
                }
                Streamer.queueFinish();
            }
        }
    }

    private static void processChunk(String line) {
        try {
            JsonObject chunk = GSON.fromJson(line, JsonObject.class);
            JsonObject delta = chunk.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("delta");

            if (delta.has("content")) {
                String partial = delta.get("content").getAsString();
                Streamer.queueChunk(partial);
            }

            if (chunk.has("x_groq")) {
                JsonObject xGroq = chunk.getAsJsonObject("x_groq");
                if (xGroq.has("usage")) {
                    String id = xGroq.get("id").getAsString();
                    JsonObject usage = xGroq.getAsJsonObject("usage");
                    int promptTokens = usage.get("prompt_tokens").getAsInt();
                    int completionTokens = usage.get("completion_tokens").getAsInt();
                    int totalTokens = usage.get("total_tokens").getAsInt();
                    double totalTime = usage.get("total_time").getAsDouble();
                    double tps = totalTokens / totalTime;
                    Streamer.setUsage(id, promptTokens, completionTokens, totalTokens, totalTime, tps);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing chunk: {}", e.getMessage());
            Streamer.onError(e.getMessage());
        }
    }

    public static void interrupt() {
        interrupt = true;
        Streamer.interrupt();
        if (activeCall != null) {
            activeCall.cancel();
        }
    }
}