package org.apex.render.states;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.apex.mixins.accessors.DrawContextAccessor;
import org.joml.Vector2f;

public abstract class SimpleShapeRenderState implements SimpleGuiElementRenderState {
    protected final RenderPipeline pipeline;
    protected final TextureSetup textureSetup;
    protected final Matrix3x2f pose;
    protected final int x, y, w, h;
    protected final int color;
    protected final int borderColor;
    protected final float borderThickness;
    protected final ScreenRect scissorArea;
    protected final ScreenRect bounds;

    protected SimpleShapeRenderState(RenderPipeline pipeline, DrawContext context, int x, int y, int w, int h, int color, int borderColor, float borderThickness) {
        this.pipeline = pipeline;
        this.textureSetup = TextureSetup.empty();
        this.pose = new Matrix3x2f(context.getMatrices());
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        this.scissorArea = ((DrawContextAccessor) context).getScissorStack().peekLast();
        this.bounds = createBounds(pose, scissorArea);
    }

    private ScreenRect createBounds(Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = createScreenRect(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public ScreenRect scissorArea() {
        return scissorArea;
    }

    @Override
    public ScreenRect bounds() {
        return bounds;
    }

    private ScreenRect createScreenRect(Matrix3x2f pose) {
        Vector2f vector2f = pose.transformPosition(x, y, new Vector2f());
        Vector2f vector2f2 = pose.transformPosition(x + w, y, new Vector2f());
        Vector2f vector2f3 = pose.transformPosition(x, y + h, new Vector2f());
        Vector2f vector2f4 = pose.transformPosition(x + w, y + h, new Vector2f());
        float f = Math.min(Math.min(vector2f.x(), vector2f3.x()), Math.min(vector2f2.x(), vector2f4.x()));
        float g = Math.max(Math.max(vector2f.x(), vector2f3.x()), Math.max(vector2f2.x(), vector2f4.x()));
        float h = Math.min(Math.min(vector2f.y(), vector2f3.y()), Math.min(vector2f2.y(), vector2f4.y()));
        float i = Math.max(Math.max(vector2f.y(), vector2f3.y()), Math.max(vector2f2.y(), vector2f4.y()));
        return new ScreenRect(MathHelper.floor(f), MathHelper.floor(h), MathHelper.ceil(g - f), MathHelper.ceil(i - h));
    }
}