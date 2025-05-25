package org.skyblockagent.utils;

import org.joml.Vector4f;

public class ColorUtils {

    public static Vector4f colorToVec4(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return new Vector4f(r, g, b, a);
    }
}
