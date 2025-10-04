package org.skyblockagent.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.skyblockagent.Skyblockagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class HttpUtils {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final String USER_AGENT = Skyblockagent.NAMESPACE + "/" + Skyblockagent.VERSION + " (" + Skyblockagent.MINECRAFT_VERSION + ")";
    private static volatile boolean interceptStreaming = false;

    public record ApiResponse(int statusCode, String body) {}

    /**
     * Sends an HTTP request.
     *
     * @param url         URL to request
     * @param method      "GET" or "POST"
     * @param requestBody Body for POST requests (ignored for GET)
     * @param token       Optional bearer token
     */
    public static CompletableFuture<@Nullable ApiResponse> sendRequest(String url, String method, String requestBody, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip")
                .header("User-Agent", USER_AGENT)
                .version(HttpClient.Version.HTTP_2)
                .uri(URI.create(url));

        // Choose method
        if ("POST".equals(method)) {
            builder = builder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json");
        } else if ("GET".equals(method)) {
            builder = builder.GET();
        }

        // Add token if present
        if (token != null) {
            builder = builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    try {
                        InputStream body = response.body();
                        String encoding = response.headers().firstValue("Content-Encoding").orElse("");
                        if ("gzip".equalsIgnoreCase(encoding)) {
                            body = new GZIPInputStream(body);
                        }

                        String responseBody = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                        return new ApiResponse(response.statusCode(), responseBody);
                    } catch (IOException e) {
                        ApiAuth.logErr("Failed while reading http request", e);
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    ApiAuth.logErr("Failed to send http request", ex);
                    return null;
                });
    }

    public static void sendStreamingRequest(String url, String requestBody, String token, Consumer<StreamEvent> onEvent) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        interceptStreaming = false;
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    int status = response.statusCode();

                    if (status != 200) {
                        try (InputStream body = response.body()) {
                            String string = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                            JsonObject json = JsonParser.parseString(string).getAsJsonObject();
                            String error = json.has("error") ? json.get("error").getAsString() : "Unknown error";
                            CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error("API error " + status + ": " + error)));
                        } catch (Exception e) {
                            CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error("API error " + status + " (failed to read body)")));
                        }
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String json = line.substring(6).trim();

                                if ("[DONE]".equals(json) || interceptStreaming) {
                                    interceptStreaming = false;
                                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Done()));
                                    break;
                                }

                                CLIENT.execute(() -> onEvent.accept(new StreamEvent.Data(json)));
                            }
                        }
                    } catch (IOException e) {
                        CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error("Streaming failed: " + e.getMessage())));
                    }
                })
                .exceptionally(ex -> {
                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error("Request failed: " + ex.getMessage())));
                    return null;
                });
    }

    public static CompletableFuture<@Nullable ApiResponse> sendPostRequest(String url, String requestBody, @Nullable String token) throws IOException, InterruptedException {
        return sendRequest(url, "POST", requestBody, token);
    }

    public static CompletableFuture<@Nullable ApiResponse> sendGetRequest(String url, String token) throws IOException, InterruptedException {
        return sendRequest(url, "GET", null, token);
    }

    public static void interceptStreaming() {
        interceptStreaming = true;
    }

    public sealed interface StreamEvent permits StreamEvent.Data, StreamEvent.Error, StreamEvent.Done {
        record Data(String json) implements StreamEvent {}
        record Error(String error) implements StreamEvent {}
        record Done() implements StreamEvent {}
    }
}