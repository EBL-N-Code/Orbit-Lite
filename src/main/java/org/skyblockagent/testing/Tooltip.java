package org.skyblockagent.testing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

//Only for testing
public class Tooltip {
    private static final Logger LOGGER = Logger.getLogger(Tooltip.class.getName());

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .executor(createOptimizedExecutor())
            .build();

    private static final Gson GSON = new Gson();
    private static final String ENDPOINT = "http://localhost:8080/";
    private static final Map<String, ItemData> items = new ConcurrentHashMap<>();
    private static final Map<String, JsonObject> values = new ConcurrentHashMap<>();
    private static final long CACHE_LIFETIME_MS = 60_000;

    // Replace CopyOnWriteArrayList with ConcurrentHashMap.KeySet for better performance
    private static final ConcurrentHashMap<String, Boolean> FETCHING = new ConcurrentHashMap<>();

    // HTTP request timeout
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final String PRICE_PREFIX = "§a";
    private static final String KEY_COLOR = "§6";
    private static final String VALUE_COLOR = "§e";
    private static final String HIGHLIGHT_COLOR = "§b";
    private static final String BAZAAR_LABEL = "Price";
    private static final String AUCTION_LABEL = "LowestBIN";
    private static final String COINS_SUFFIX = " coins";

    public static JsonObject getValue(String itemUUID) {
        return values.get(itemUUID);
    }

    public static List<Text> formatJsonTooltip(JsonObject jsonObject) {
        List<Text> tooltipLines = new ArrayList<>();
        formatJsonRecursive(jsonObject, tooltipLines, "");
        return tooltipLines;
    }

    private static ThreadPoolExecutor createOptimizedExecutor() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        executor.setKeepAliveTime(60, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            String itemId = stack.getItemId();
            if (itemId == null || itemId.isEmpty()) return;

            try {
                processItemTooltip(stack, itemId, lines);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing tooltip for item " + itemId, e);
            }
        });
    }

    private static void processItemTooltip(ItemStack stack, String itemId, List<Text> lines) {
        ItemData item = items.get(itemId);
        if (item != null && item.stillValid()) {
            addPriceTooltip(item, lines);

            if ("auction".equals(item.source)) {
                processAuctionTooltip(stack, lines);
            }
        } else {
            fetchItemDataIfNotInProgress(itemId);
        }
    }

    private static void addPriceTooltip(ItemData item, List<Text> lines) {
        String source = item.source.equals("bazaar") ? BAZAAR_LABEL : AUCTION_LABEL;
        lines.add(Text.literal(PRICE_PREFIX + source + ": " +
                FormattingUtils.formatPrice(item.price) + COINS_SUFFIX));
    }

    private static void processAuctionTooltip(ItemStack stack, List<Text> lines) {
        String itemUUID = stack.getItemUUID();
        if (itemUUID == null || itemUUID.isEmpty()) return;

        JsonObject value = values.get(itemUUID);
        if (value == null) {
            fetchValueIfNotInProgress(itemUUID);
        }
    }

    private static void fetchItemDataIfNotInProgress(String itemId) {
        fetchDataIfNotInProgress(itemId, () -> fetchPrice(itemId));
    }

    private static void fetchValueIfNotInProgress(String itemUUID) {
        fetchDataIfNotInProgress(itemUUID, () -> fetchValue(itemUUID));
    }

    private static void fetchDataIfNotInProgress(String id, Runnable fetcher) {
        if (FETCHING.putIfAbsent(id, true) == null && HTTP.executor().isPresent()) {
            CompletableFuture.runAsync(fetcher, HTTP.executor().get())
                    .exceptionally(throwable -> {
                        LOGGER.log(Level.SEVERE, "Failed to fetch data for " + id, throwable);
                        FETCHING.remove(id);
                        return null;
                    });
        }
    }

    private static void formatJsonRecursive(JsonObject jsonObject, List<Text> lines, String indent) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if ("modifiers".equals(key) && value.isJsonObject()) {
                lines.add(Text.literal(indent + KEY_COLOR + "Modifiers:"));
                formatModifiers(value.getAsJsonObject(), lines, indent + "  ");
            } else if (value.isJsonObject()) {
                lines.add(Text.literal(indent + KEY_COLOR + key + ":"));
                formatJsonRecursive(value.getAsJsonObject(), lines, indent + "  ");
            } else if (value.isJsonPrimitive()) {
                String displayValue;
                if (value.getAsJsonPrimitive().isNumber() && (key.toLowerCase().contains("price") || key.toLowerCase().contains("value"))) {
                    displayValue = FormattingUtils.formatPrice(value.getAsDouble());
                } else {
                    displayValue = value.getAsString();
                }
                lines.add(Text.literal(indent + VALUE_COLOR + key + ": " + HIGHLIGHT_COLOR + displayValue));
            }
        }
    }

    private static void formatModifiers(JsonObject modifiersObject, List<Text> lines, String indent) {
        for (Map.Entry<String, JsonElement> entry : modifiersObject.entrySet()) {
            String id = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();

            int count = data.has("count") ? data.get("count").getAsInt() : 1;

            StringBuilder sb = new StringBuilder();
            sb.append(indent).append(KEY_COLOR).append(id);
            if (count > 1) {
                sb.append(" ").append(VALUE_COLOR).append(count).append(" x");
            }

            boolean hasIngredients = data.has("ingredients") && data.get("ingredients").isJsonObject() && !data.getAsJsonObject("ingredients").entrySet().isEmpty();

            if (hasIngredients) {
                lines.add(Text.literal(sb.toString()));
                formatModifiers(data.getAsJsonObject("ingredients"), lines, indent + "  ");
            } else {
                double price = 0;
                if (data.has("price") && !data.get("price").isJsonNull()) {
                    price = data.get("price").getAsDouble();
                }
                sb.append(": ").append(HIGHLIGHT_COLOR).append(FormattingUtils.formatPrice(price));
                lines.add(Text.literal(sb.toString()));
            }
        }
    }

    private static void fetchPrice(String itemId) {
        try {
            String url = ENDPOINT + "price?item_id=" + URLEncoder.encode(itemId, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(REQUEST_TIMEOUT)
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ItemData itemData = GSON.fromJson(response.body(), ItemData.class);
                if (itemData != null && itemData.item_id() != null && !itemData.item_id().isEmpty()) {
                    items.put(itemId, itemData);
                    LOGGER.info("Fetched price for: " + itemId);
                } else {
                    LOGGER.warning("No price data found for item ID: " + itemId);
                }
            } else {
                LOGGER.warning("Failed to fetch price for " + itemId + ". Status: " + response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching price for " + itemId, e);
        } finally {
            FETCHING.remove(itemId);
        }
    }

    private static void fetchValue(String itemUUID) {
        try {
            String url = ENDPOINT + "auction/" + URLEncoder.encode(itemUUID, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(REQUEST_TIMEOUT)
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject itemData = GSON.fromJson(response.body(), JsonObject.class);
                if (itemData != null && !itemData.isJsonNull() && !itemData.isEmpty()) {
                    values.put(itemUUID, itemData);
                    LOGGER.info("Fetched value for: " + itemUUID);
                } else {
                    LOGGER.warning("No value data found for item UUID: " + itemUUID);
                }
            } else {
                LOGGER.warning("Failed to fetch value for " + itemUUID + ". Status: " + response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching value for " + itemUUID, e);
        } finally {
            FETCHING.remove(itemUUID);
        }
    }

    private record ItemData(String item_id, String auction_id, double price, String source, long timestamp) {
        boolean stillValid() {
            return System.currentTimeMillis() - timestamp <= CACHE_LIFETIME_MS;
        }
    }
}
