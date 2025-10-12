package org.skyblockagent.render.states;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.skyblockagent.mixins.accessors.DrawContextAccessor;

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
        this.bounds = createBounds(x, y, w, h, pose, scissorArea);
    }

    @Nullable
    private static ScreenRect createBounds(int x, int y, int width, int height, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = new ScreenRect(x, y, width, height).transformEachVertex(pose);
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
}