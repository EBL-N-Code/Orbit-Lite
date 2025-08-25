package org.skyblockagent.testing;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemUtils {

    @SuppressWarnings("deprecation")
    public static @NotNull NbtCompound getCustomData(@NotNull ComponentHolder itemStack) {
        return itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
    }

    public static String getItemId(ComponentHolder itemStack) {
        NbtCompound customData = getCustomData(itemStack);
        Optional<String> id = customData.getString("id");
        if (id.isPresent()) {
            String itemId = id.get();
            if (itemId.isEmpty()) return itemId;

            return ItemIdResolver.resolveItemId(itemId, customData);
        }
        return "";
    }

    public static String getItemUUID(ComponentHolder itemStack) {
        NbtCompound customData = getCustomData(itemStack);
        Optional<String> uuid = customData.getString("uuid");
        return uuid.orElse("");
    }
}
