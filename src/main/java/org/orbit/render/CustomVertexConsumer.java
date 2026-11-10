package org.orbit.render;

import net.minecraft.client.render.VertexConsumer;

public abstract class CustomVertexConsumer implements VertexConsumer {
    @SuppressWarnings("unused")
    public abstract VertexConsumer lineWidth(float width);
}
