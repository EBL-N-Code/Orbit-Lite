package org.skyblockagent.testing;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.mixins.accessors.HandledScreenAccessor;

import java.util.List;

public class ValueHud {
    private static final Identifier LAYER = Identifier.of(Skyblockagent.NAMESPACE, "test_layer");

    public static void register() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, LAYER, ValueHud::render));
    }

    private static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = client.currentScreen;

        if (!(screen instanceof HandledScreen<?> handledScreen)) {
            return;
        }

        Slot hoveredSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();

        if (hoveredSlot == null || !hoveredSlot.hasStack()) {
            return;
        }

        ItemStack stack = hoveredSlot.getStack();
        String itemUUID = stack.getItemUUID();

        if (itemUUID == null || itemUUID.isEmpty()) {
            return;
        }

        JsonObject value = Tooltip.getValue(itemUUID);
        if (value != null) {
            List<Text> tooltipLines = Tooltip.formatJsonTooltip(value);
            int y = 20;
            for (Text line : tooltipLines) {
                context.drawTextWithShadow(client.textRenderer, line, 20, y, 0xFFFFFF);
                y += 10;
            }
        }
    }
}
