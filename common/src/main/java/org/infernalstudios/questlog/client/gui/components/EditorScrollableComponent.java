package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.gui.components.events.GuiEventListener;
import org.infernalstudios.questlog.client.gui.components.scrollable.Scrollable;

/** Give nested text editors first use of the wheel and always finish their drag. */
public class EditorScrollableComponent extends ScrollableComponent {
    public EditorScrollableComponent(int x, int y, int width, int height, Scrollable scrollable) {
        super(x, y, width, height, scrollable);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY) && scrollable instanceof GuiEventListener listener
                && listener.mouseScrolled(mouseX - getXOffset(), mouseY - getYOffset(), amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = scrollable instanceof GuiEventListener listener
                && listener.mouseReleased(mouseX - getXOffset(), mouseY - getYOffset(), button);
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }
}
