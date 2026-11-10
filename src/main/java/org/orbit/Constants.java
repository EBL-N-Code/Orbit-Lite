package org.orbit;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Constants {
    public static final Text PREFIX = Text.literal("[").formatted(Formatting.DARK_AQUA).append(Text.literal(Orbit.MOD_NAME).formatted(Formatting.AQUA)).append(Text.literal("] ").formatted(Formatting.DARK_AQUA));
    public static final String UPGRADE_URL = "https://apexmod.tech/";
    public static final String SERVER_IP = "https://api.apexmod.tech";

    public static final Text IMPORT_FAILED = Text.literal("Failed to import your content.").formatted(Formatting.RED);
    public static final Text REPORT_ISSUE = Text.literal("Please report this issue.").formatted(Formatting.RED);
    public static final String MOD_MAINTENANCE = "The mod is currently unavailable due to maintenance.";
    public static final String MOD_DISABLED = "Failed to import your content. Please report this issue.";
    public static final String INPUT_PLACEHOLDER = "Ask anything";
    public static final String NEW_CHAT = "+ New Chat";
    public static final String CHATS = "Chats";
    public static final String WELCOME = "Welcome back";
}
