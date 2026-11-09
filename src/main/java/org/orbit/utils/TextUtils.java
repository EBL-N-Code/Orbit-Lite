package org.apex.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextUtils {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final NumberFormat FORMATTER;
    private static final Style EMPTY_STYLE = Style.EMPTY;

    static {
        FORMATTER = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        FORMATTER.setMinimumFractionDigits(0);
        FORMATTER.setMaximumFractionDigits(1);
    }

    public static String trimString(String text, int maxWidth) {
        if (TEXT_RENDERER.getWidth(text) <= maxWidth) return text;

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
        return text.substring(0, cutOff).trim() + ellipsis;
    }

    public static List<String> wrapLines(String line, int maxWidth) {
        List<String> list = new ArrayList<>();
        TEXT_RENDERER.getTextHandler().wrapLines(
                line,
                maxWidth,
                EMPTY_STYLE,
                true,
                (ignored, start, end) -> list.add(line.substring(start, end))
        );
        return list;
    }

    public static String formatNumber(int number) {
        return FORMATTER.format(number).toLowerCase();
    }
}
