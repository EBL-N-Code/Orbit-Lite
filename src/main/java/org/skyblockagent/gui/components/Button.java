package org.skyblockagent.gui.components;

import net.minecraft.client.gui.DrawContext;

public abstract class Button {
    protected int x, y, width, height;

    public Button() {}

    public Button(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void onClick();
    public abstract void render(DrawContext context, double mouseX, double mouseY);

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getBottom() {
        return y + height;
    }
}
