package org.skyblockagent.utils.scheduler;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Supplier;

public class Scheduler {

    /**
     * Opens a screen on the next tick.
     *
     * @param screenSupplier A supplier that provides the screen to open.
     */
    public static Command<FabricClientCommandSource> openScreen(Supplier<Screen> screenSupplier) {
        return _ -> openScreen(screenSupplier.get());
    }

    private static int openScreen(Screen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }
}
