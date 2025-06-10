package org.skyblockagent.chat.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.skyblockagent.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextLine {
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private static final Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*|__(.*?)__");
    private final int maxWidth;
    private final List<MutableText> lines = new ArrayList<>();
    private String rawText;
    private String buffer;
    private int height;

    public TextLine(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void appendText(String text) {
        rawText = text;
        buffer = text;
        parse();
    }

    private void parse() {
        height = 0;
        for (String line : TextUtils.wrapLines(buffer, maxWidth)) {
            lines.add(Text.literal(line));
            height += 10;
        }
    }

    public List<MutableText> getText() {
        return lines;
    }

    public String getRawText() {
        return rawText;
    }
}