package org.orbit.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.orbit.Orbit;
import org.jetbrains.annotations.Nullable;
import org.orbit.utils.Logger;

import java.io.BufferedReader;
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
    private static final String USER_AGENT = Orbit.NAMESPACE + "/" + Orbit.VERSION + " (" + Orbit.MINECRAFT_VERSION + ")";
    private static volatile CompletableFuture<?> streamingFuture;

    public record ApiResponse(int statusCode, String body) {}

    /**
     * Sends an HTTP request.
     *
     * @param url         URL to request
     * @param method      "GET" or "POST"
     * @param requestBody Body for POST requests (ignored for GET)
     * @param token       Optional bearer token
     */
    public static CompletableFuture<@Nullable ApiResponse> sendRequest(String url, String method, String requestBody, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip")
                .header("User-Agent", USER_AGENT)
                .version(HttpClient.Version.HTTP_2)
                .uri(URI.create(url));

        // Choose method
        builder = switch (method) {
            case "POST" -> builder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json");
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
            default -> throw new IllegalStateException("Unexpected request method: " + method);
        };

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
                    } catch (Exception e) {
                        Logger.error("Failed to send request", e);
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    Logger.error("Failed to send request", ex);
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

        streamingFuture = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    int status = response.statusCode();

                    if (status != 200) {
                        try (InputStream body = response.body()) {
                            String string = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                            JsonObject json = JsonParser.parseString(string).getAsJsonObject();
                            String error = json.has("error") ? json.get("error").getAsString() : "Unknown error";
                            CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error(null, error)));
                        } catch (Exception e) {
                            CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error(null, "API returned " + status + " code: (failed to read body)")));
                        }
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                        while (!Thread.currentThread().isInterrupted()) {
                            String line = reader.readLine();
                            if (line == null) break;
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if (data.startsWith("content: ")) {
                                    String content = data.substring(9);
                                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Data(content)));
                                    continue;
                                }

                                if (data.startsWith("{\"chat_info\":")) {
                                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Info(data)));
                                    continue;
                                }

                                if (data.startsWith("{\"usage\":")) {
                                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Done(data)));
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error(e, "Streaming Error: " + e.getMessage())));
                    }
                })
                .exceptionally(e -> {
                    CLIENT.execute(() -> onEvent.accept(new StreamEvent.Error(e, "Streaming Error: " + e.getMessage())));
                    return null;
                });
    }

    public static CompletableFuture<@Nullable ApiResponse> sendPostRequest(String url, String requestBody, @Nullable String token) {
        return sendRequest(url, "POST", requestBody, token);
    }

    public static CompletableFuture<@Nullable ApiResponse> sendGetRequest(String url, String token) {
        return sendRequest(url, "GET", null, token);
    }

    public static void interceptStreaming() {
        BackendService.activeCall = false;
        if (streamingFuture != null) {
            streamingFuture.cancel(true);
            streamingFuture = null;
        }
    }

    public sealed interface StreamEvent permits StreamEvent.Data, StreamEvent.Info, StreamEvent.Done, StreamEvent.Error {
        record Data(String content) implements StreamEvent {}
        record Info(String content) implements StreamEvent {}
        record Done(String content) implements StreamEvent {}
        record Error(Throwable throwable, String error) implements StreamEvent {}
    }
}