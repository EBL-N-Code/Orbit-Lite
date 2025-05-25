package org.skyblockagent.utils.render;

import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * An interface for render layers that support setting custom uniforms for shader programs.
 * Extends {@link net.minecraft.client.render.RenderLayer} functionality to allow dynamic
 * uniform updates during rendering.
 */
public interface UniformRenderLayer {

    /**
     * Sets a uniform with the specified name and 2D vector value.
     *
     * @param name  The name of the uniform.
     * @param value The {@link Vector2f} value to set.
     */
    default void setUniform(String name, Vector2f value) {
        setUniform(name, new float[]{value.x(), value.y()});
    }

    /**
     * Sets a uniform with the specified name and 4D vector value.
     *
     * @param name  The name of the uniform.
     * @param value The {@link Vector4f} value to set.
     */
    default void setUniform(String name, Vector4f value) {
        setUniform(name, new float[]{value.x(), value.y(), value.z(), value.w()});
    }

    /**
     * Sets a uniform with the specified name and value.
     *
     * @param name  The name of the uniform.
     * @param value The value to set (e.g., Float, float[], Matrix4f).
     */
    void setUniform(String name, Object value);
}