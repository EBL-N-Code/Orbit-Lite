package org.skyblockagent.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.MinecraftClient;
import okhttp3.*;
import org.skyblockagent.managers.message.ChatMessage;
import org.skyblockagent.managers.message.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AIClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIClient.class);
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();
    private static OkHttpClient client;

    public static void initClient() {
        client = new OkHttpClient();
    }

    public static void sendRequest(AIRequest request, List<ChatMessage> messages) {
        MinecraftClient.getInstance().executeAsync(_ -> CompletableFuture.runAsync(() -> {
            try {
                JsonObject payload = buildPayload(messages);
                Request httpRequest = buildHttpRequest(payload);
                processResponse(httpRequest, request);
            } catch (Exception e) {
                LOGGER.error("API error: {}", e.getMessage(), e);
                request.onError(e.getMessage());
            }
        }));
    }

    private static JsonObject buildPayload(List<ChatMessage> chatMessages) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "meta-llama/llama-4-maverick-17b-128e-instruct");
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        for (ChatMessage chatMessage : chatMessages) {
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", chatMessage.getSender() == MessageSender.AI ? "assistant" : "user");
            userMessage.addProperty("content", chatMessage.getContent());
            messages.add(userMessage);
        }
        payload.add("messages", messages);

        return payload;
    }

    private static Request buildHttpRequest(JsonObject payload) {
        RequestBody body = RequestBody.create(GSON.toJson(payload), JSON);
        return new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private static void processResponse(Request httpRequest, AIRequest request) throws IOException {
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("API request failed with code: {}", response.code());
                throw new RuntimeException("API request failed with code: " + response.code());
            }

            if (response.body() == null) {
                LOGGER.error("Response body is null");
                throw new IOException("No response body");
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
                    processChunk(line, request);
                }
                request.onFinish();
            }
        }
    }

    private static void processChunk(String line, AIRequest request) {
        try {
            JsonObject chunk = GSON.fromJson(line, JsonObject.class);
            JsonObject delta = chunk.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("delta");

            if (delta.has("content")) {
                String partial = delta.get("content").getAsString();
                request.onPartial(partial);
            }

            if (chunk.has("usage")) {
                JsonObject xGroq = chunk.getAsJsonObject("x_groq");
                String id = xGroq.get("id").getAsString();
                JsonObject usage = chunk.getAsJsonObject("usage");
                int prompt = usage.get("prompt_tokens").getAsInt();
                int completion = usage.get("completion_tokens").getAsInt();
                int total = usage.get("total_tokens").getAsInt();
                double time = usage.get("total_time").getAsDouble();
                request.setUsage(id, prompt, completion, total, time);
            }
        } catch (JsonParseException e) {
            LOGGER.error("Error parsing chunk: {}", e.getMessage());
        }
    }
}