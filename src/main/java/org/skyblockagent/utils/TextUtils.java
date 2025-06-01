package org.skyblockagent.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class TextUtils {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;

    public static String trimString(String text, int maxWidth) {
        if (TEXT_RENDERER.getWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = TEXT_RENDERER.getWidth(ellipsis);

        // Binary search to find the longest prefix that fits
        int low = 0;
        int high = text.length();

        while (low < high) {
            int mid = (low + high) / 2;
            String sub = text.substring(0, mid);
            if (TEXT_RENDERER.getWidth(sub) + ellipsisWidth <= maxWidth) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        // Ensure we don't go out of bounds
        int cutOff = Math.max(0, low - 1);
        return text.substring(0, cutOff) + ellipsis;
    }
}
