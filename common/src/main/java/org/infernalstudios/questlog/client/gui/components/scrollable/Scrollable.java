package org.infernalstudios.questlog.client.gui.components.scrollable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;

public interface Scrollable extends Renderable {
    int getHeight();

    default void setScrollableComponent(ScrollableComponent component) {
    }

    default void renderBackground(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
    }
}
