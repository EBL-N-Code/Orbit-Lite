package org.skyblockagent.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.skyblockagent.mixins.accessors.BufferBuilderAccessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectVertexConsumer implements VertexConsumer {
    private final BufferBuilderAccessor accessor;
    private final VertexFormat format;
    private ByteBuffer into;

    public DirectVertexConsumer(BufferBuilder original) {
        this.accessor = (BufferBuilderAccessor) original;
        format = accessor.getVertexFormat();
        newVert();
    }

    private static byte floatToByte(float f) {
        return (byte) ((int) (MathHelper.clamp(f, -1.0F, 1.0F) * 127.0F) & 0xFF);
    }

    private void checkEnd() {
        if (!into.hasRemaining()) newVert();
    }

    private void newVert() {
        into = MemoryUtil.memByteBuffer(accessor.beginNewVertex(), format.getVertexSize());
        into.order(ByteOrder.nativeOrder());
    }

    @Override
    public DirectVertexConsumer vertex(Matrix3x2f matrix, float x, float y) {
        return (DirectVertexConsumer) VertexConsumer.super.vertex(matrix, x, y);
    }

    @Override
    public DirectVertexConsumer vertex(float x, float y, float z) {
        checkEnd();
        into.putFloat(x);
        into.putFloat(y);
        into.putFloat(z);
        return this;
    }

    @Override
    public DirectVertexConsumer color(int red, int green, int blue, int alpha) {
        return color(ColorHelper.getArgb(alpha, red, green, blue));
    }

    @Override
    public DirectVertexConsumer color(int argb) {
        checkEnd();
        into.putInt(ColorHelper.toAbgr(argb));
        return this;
    }

    public DirectVertexConsumer putColor(int argb) {
        checkEnd();
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        into.putFloat(r);
        into.putFloat(g);
        into.putFloat(b);
        into.putFloat(a);
        return this;
    }

    public DirectVertexConsumer putFloat(float f) {
        checkEnd();
        into.putFloat(f);
        return this;
    }

    public DirectVertexConsumer putVector4f(Vector4f vector4f) {
        checkEnd();
        into.putFloat(vector4f.x);
        into.putFloat(vector4f.y);
        into.putFloat(vector4f.z);
        into.putFloat(vector4f.w);
        return this;
    }

    @Override
    public DirectVertexConsumer texture(float u, float v) {
        checkEnd();
        into.putFloat(u);
        into.putFloat(v);
        return this;
    }

    @Override
    public DirectVertexConsumer overlay(int u, int v) {
        checkEnd();
        into.putShort((short) u);
        into.putShort((short) v);
        return this;
    }

    @Override
    public DirectVertexConsumer overlay(int uv) {
        checkEnd();
        into.putInt(uv);
        return this;
    }

    @Override
    public DirectVertexConsumer light(int u, int v) {
        return overlay(u, v);
    }

    @Override
    public DirectVertexConsumer light(int uv) {
        return overlay(uv);
    }

    @Override
    public DirectVertexConsumer normal(float x, float y, float z) {
        checkEnd();
        into.put(floatToByte(x));
        into.put(floatToByte(y));
        into.put(floatToByte(z));
        return this;
    }
}