package org.apex.mixins.accessors;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Invoker("beginVertex")
    long beginNewVertex();

    @Accessor("vertexFormat")
    VertexFormat getVertexFormat();
}
