package org.skyblockagent.utils.render;

import org.joml.Vector2f;

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
        float[] array = new float[]{value.x(), value.y()};
        setUniform(name, array);
    }

    /**
     * Sets a uniform with the specified name and value.
     *
     * @param name  The name of the uniform.
     * @param value The value to set (e.g., Float, float[], Matrix4f).
     */
    void setUniform(String name, Object value);
}