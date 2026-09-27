package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.ScrollbarTexture;

/** A multiline editor using Questlog's scrollbar, including its padded texture. */
public class QuestTextEditBox extends MultiLineEditBox {
    private boolean draggingScrollbar;

    public QuestTextEditBox(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty(), Component.empty());
    }

    private boolean overScrollbar(double mouseX, double mouseY) {
        return scrollbarVisible() && mouseX >= getX() + getWidth()
                && mouseX < getX() + getWidth() + 8
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || (visible && overScrollbar(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && button == 0 && overScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (visible && button == 0 && draggingScrollbar) {
            scrollToMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = button == 0 && draggingScrollbar;
        if (button == 0) draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!visible || !isMouseOver(mouseX, mouseY) || !scrollbarVisible()) return false;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private int thumbHeight() {
        return QuestlogGuiSet.DEFAULT.scrollbar.bar().height() - 24;
    }

    private void scrollToMouse(double mouseY) {
        int travel = getHeight() - thumbHeight();
        if (travel > 0) {
            setScrollAmount(getMaxScrollAmount() * (mouseY - getY() - thumbHeight() / 2.0) / travel);
        }
    }

    @Override
    protected void renderDecorations(GuiGraphics graphics) {
        if (!scrollbarVisible()) return;
        ScrollbarTexture scrollbar = QuestlogGuiSet.DEFAULT.scrollbar;
        int x = getX() + getWidth() - 10;
        int bottom = getY() + getHeight();
        graphics.enableScissor(getX() + getWidth(), getY(), getX() + getWidth() + 8, bottom);
        int backgroundX = x + (scrollbar.bar().width() - scrollbar.background().width()) / 2;
        scrollbar.backgroundTopCap().blit(graphics, backgroundX, getY());
        for (int y = getY() + scrollbar.backgroundTopCap().height(); y < bottom - scrollbar.backgroundBottomCap().height(); y += scrollbar.background().height()) {
            scrollbar.background().blit(graphics, backgroundX, y);
        }
        scrollbar.backgroundBottomCap().blit(graphics, backgroundX, bottom - scrollbar.backgroundBottomCap().height());
        int thumbY = getY() + (int) Math.round(scrollAmount() * (getHeight() - thumbHeight()) / Math.max(1, getMaxScrollAmount()));
        scrollbar.bar().blit(graphics, x, thumbY - 12);
        graphics.disableScissor();
    }
}
