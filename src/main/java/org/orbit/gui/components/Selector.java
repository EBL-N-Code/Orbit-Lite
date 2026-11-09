package org.apex.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.apex.gui.components.dropdown.Dropdown;

public abstract class Selector {
    protected final int x, y;
    protected boolean hovered;
    protected Dropdown dropdown;

    public Selector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void renderButton(DrawContext context, int mouseX, int mouseY);
    public abstract void createDropdown();

    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderButton(context, mouseX, mouseY);
        if (dropdown != null) {
            dropdown.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isOpen() {
        return dropdown != null;
    }

    public void close() {
        dropdown = null;
    }

    public Dropdown getDropdown() {
        return dropdown;
    }
}