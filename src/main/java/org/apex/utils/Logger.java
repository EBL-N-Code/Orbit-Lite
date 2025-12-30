package org.apex.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.apex.Constants;
import org.apex.config.ModState;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apex.Apex.MOD_NAME;

public class Logger {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    private final static List<Text> PENDING_NOTIFICATIONS = new ArrayList<>();

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, client) -> {
            if (client.player != null && !PENDING_NOTIFICATIONS.isEmpty()) {
                Scheduler.getInstance().schedule(() -> {
                    for (Text notif : PENDING_NOTIFICATIONS) {
                        sendToPlayer(notif);
                    }
                    PENDING_NOTIFICATIONS.clear();
                }, 20);
            }
        });
    }

    public static void error(String error, Object... exceptions) {
        LOGGER.error(error, exceptions);
    }

    public static void error(String error, Object exception, boolean disableMod) {
        if (disableMod) ModState.disableMod();
        LOGGER.error(error, exception);
    }

    public static void sendToPlayer(Text message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Constants.PREFIX.copy().append(message), false);
        } else {
            if (PENDING_NOTIFICATIONS.isEmpty() || !PENDING_NOTIFICATIONS.getLast().equals(message)) {
                PENDING_NOTIFICATIONS.add(message);
            }
        }
    }
}