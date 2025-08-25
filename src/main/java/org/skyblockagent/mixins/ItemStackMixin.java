package org.skyblockagent.mixins;

import net.minecraft.component.ComponentHolder;
import net.minecraft.item.ItemStack;
import org.skyblockagent.testing.ItemUtils;
import org.skyblockagent.testing.SkyblockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements SkyblockItem {

    @Unique
    private String itemId;

    @Unique
    private String itemUUID;

    @Unique
    @Override
    public String getItemId() {
        if (itemId != null) return itemId;
        return itemId = ItemUtils.getItemId((ComponentHolder) this);
    }

    @Unique
    @Override
    public String getItemUUID() {
        if (itemUUID != null) return itemUUID;
        return itemUUID = ItemUtils.getItemUUID((ComponentHolder) this);
    }
}