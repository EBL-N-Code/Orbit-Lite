package org.skyblockagent.testing;

import java.text.NumberFormat;
import java.util.Locale;

public class FormattingUtils {
    private static final NumberFormat COMPACT_NUMBER_FORMATTER = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);

    static {
        COMPACT_NUMBER_FORMATTER.setMaximumFractionDigits(2);
    }

    public static String formatPrice(double price) {
        return COMPACT_NUMBER_FORMATTER.format(price);
    }
}
