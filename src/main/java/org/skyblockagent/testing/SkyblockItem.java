package org.skyblockagent.testing;

public interface SkyblockItem {

    default String getItemId() {
        return "";
    }

    default String getItemUUID() {
        return "";
    }
}
