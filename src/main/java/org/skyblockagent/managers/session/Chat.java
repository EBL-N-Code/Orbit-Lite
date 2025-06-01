package org.skyblockagent.managers.session;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.UUID;

public class Chat {
    private final UUID id;
    private final String title;
    private long lastModified;

    public Chat(UUID id, String title, long lastModified) {
        this.id = id;
        this.title = title;
        this.lastModified = lastModified;
    }

    public void update(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}