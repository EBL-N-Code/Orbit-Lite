package org.orbit.gui.header;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import org.orbit.chat.ChatManager;
import org.orbit.gui.ChatScreen;
import org.orbit.gui.components.Selector;
import org.orbit.gui.components.Widget;
import org.orbit.gui.components.dropdown.Dropdown;
import org.orbit.render.Renderer;

import java.util.ArrayList;
import java.util.List;

public class Header extends Widget {
    private List<Selector> selectors;

    @Override
    public void init() {
        selectors = new ArrayList<>();
        selectors.add(new ProfileViewer(x + width - ProfileViewer.SIZE - 8, y + (height / 2) - (ProfileViewer.SIZE / 2) + 2));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        String title = ChatManager.getInstance().getChat().getName();
        if (title != null) {
            int x = ChatScreen.getInstance().getLeft() + (ChatScreen.getInstance().getChatWidth() / 2);
            Renderer.drawCenteredText(context, title, x, y + (height / 2) - 3);
        }

        for (Selector selector : selectors) {
            selector.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    public boolean dropdownClicked() {
        for (Selector selector : selectors) {
            if (selector.isOpen()) {
                if (selector.isHovered()) {
                    selector.close();
                    return true;
                }

                Dropdown dropdown = selector.getDropdown();
                if (dropdown.isHovered()) {
                    if (dropdown.handleClick()) {
                        selector.close();
                    }
                    return true;
                }
                selector.close();
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (Selector selector : selectors) {
            if (selector.isHovered()) {
                selector.createDropdown();
                return true;
            }
        }

        return false;
    }
}
