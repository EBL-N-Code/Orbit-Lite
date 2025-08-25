package org.skyblockagent.testing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ItemIdResolver {
    private static final Map<String, Function<NbtCompound, String>> ITEM_ID_RESOLVERS = new HashMap<>();

    static {
        // Register handlers for specific IDs
        ITEM_ID_RESOLVERS.put("ENCHANTED_BOOK", customData -> {
            if (customData.contains("enchantments")) {
                Optional<NbtCompound> enchants = customData.getCompound("enchantments");
                if (enchants.isPresent()) {
                    String enchant = enchants.get().getKeys().stream().findFirst().orElse("");
                    return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.get().getInt(enchant);
                }
            }
            return "ENCHANTED_BOOK";
        });

        ITEM_ID_RESOLVERS.put("ATTRIBUTE_SHARD", customData -> {
            if (customData.contains("attributes")) {
                Optional<NbtCompound> shards = customData.getCompound("attributes");
                if (shards.isPresent()) {
                    String shard = shards.get().getKeys().stream().findFirst().orElse("");
                    return "ATTRIBUTE_SHARD_" + shard.toUpperCase(Locale.ENGLISH) + "_" + shards.get().getInt(shard);
                }
            }
            return "ATTRIBUTE_SHARD";
        });

        ITEM_ID_RESOLVERS.put("PET", customData -> {
            if (customData.contains("petInfo")) {
                Optional<String> petInfoJson = customData.getString("petInfo");
                if (petInfoJson.isPresent()) {
                    JsonObject petInfo = JsonParser.parseString(petInfoJson.get()).getAsJsonObject();
                    String type = petInfo.get("type").getAsString();
                    String tier = petInfo.get("tier").getAsString();
                    return type + "_" + tier;
                }
            }
            return "PET";
        });

        ITEM_ID_RESOLVERS.put("RUNE", customData -> {
            if (customData.contains("runes")) {
                Optional<NbtCompound> runes = customData.getCompound("runes");
                if (runes.isPresent()) {
                    String rune = runes.get().getKeys().stream().findFirst().orElse("");
                    return rune + "_RUNE_" + runes.get().getInt(rune);
                }
            }
            return "RUNE";
        });

        ITEM_ID_RESOLVERS.put("POTION", customData -> {
            if (customData.contains("potion") && customData.contains("potion_level")) {
                Optional<String> potion = customData.getString("potion");
                if (potion.isPresent()) {
                    return "POTION_" + potion.get().toUpperCase(Locale.ENGLISH)
                            + "_" + customData.getInt("potion_level");
                }
            }
            return "POTION";
        });
    }

    /**
     * Resolves a custom item ID based on the given NBT data.
     *
     * @param id         The base ID of the item.
     * @param customData The NBT data containing item details.
     * @return The resolved custom item ID.
     */
    public static String resolveItemId(String id, NbtCompound customData) {
        Function<NbtCompound, String> resolver = ITEM_ID_RESOLVERS.get(id);
        if (resolver != null) {
            return resolver.apply(customData);
        }
        return id; // Default to the raw ID if no specific resolver exists
    }
}