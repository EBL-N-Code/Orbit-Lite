package org.apex.utils;

public class ColorUtils {
    private static final int BACKGROUND_OPACITY = 100;

    public static int applyBgOpacity(int color) {
        int alpha = (int) (BACKGROUND_OPACITY / 100.0 * 255);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static int applyOpacity(int color, int opacity) {
        int alpha = (int) (opacity / 100.0 * 255);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
