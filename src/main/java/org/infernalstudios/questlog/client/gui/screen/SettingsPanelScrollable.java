package org.infernalstudios.questlog.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.scrollable.Scrollable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SettingsPanelScrollable implements Scrollable, GuiEventListener, NarratableEntry {
    private final QuestEditorScreen screen;
    private ScrollableComponent scroller;
    private AbstractWidget focusedWidget = null;

    public SettingsPanelScrollable(QuestEditorScreen screen) {
        this.screen = screen;
    }

    private int rowHeight(int index) {
        return this.screen.settingsRowHeights.get(index);
    }

    private int rowY(int index) {
        int y = 0;
        for (int i = 0; i < index; i++) {
            y += rowHeight(i);
        }
        return y;
    }

    @Override
    public int getHeight() {
        return rowY(this.screen.settingsFields.size()) + 8;
    }

    @Override
    public void setScrollableComponent(ScrollableComponent component) {
        this.scroller = component;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        if (this.scroller == null) return;

        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (screen.width - totalWidth) / 2;
        int baseY = (screen.height - height) / 2;
        int panel2X = baseX + leftWidth + PANEL_SPACING;
        int panel2Y = baseY;

        int startX = panel2X + 18;
        int startY = panel2Y + 28;
        int scroll = (int) this.scroller.getScrollAmount();

        int absMouseX = mouseX + (int) this.scroller.getXOffset();
        int absMouseY = mouseY + (int) this.scroller.getYOffset();
        boolean hovered = this.scroller.isMouseOver(absMouseX, absMouseY);
        int renderMouseX = hovered ? absMouseX : -9999;
        int renderMouseY = hovered ? absMouseY : -9999;

        int color = Questlog.getConfig().colors.textColor | 0xFF000000;

        List<AbstractWidget> fields = this.screen.settingsFields;
        List<String> labels = this.screen.settingsLabels;
        for (int i = 0; i < fields.size(); i++) {
            AbstractWidget widget = fields.get(i);
            int y = startY + rowY(i) - scroll;
            String labelKey = labels.get(i);

            if (labelKey != null) {
                ps.drawString(screen.getFont(), Component.translatable(labelKey), startX, y, color, false);
                widget.setX(startX);
                widget.setY(y + 10);
            } else {
                widget.setX(startX);
                widget.setY(y);
            }
            widget.render(ps, renderMouseX, renderMouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        if (!this.scroller.isMouseOver(absoluteX, absoluteY)) {
            return false;
        }

        boolean anyClicked = false;
        AbstractWidget clickedWidget = null;
        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.mouseClicked(absoluteX, absoluteY, button)) {
                anyClicked = true;
                clickedWidget = widget;
            }
        }

        for (AbstractWidget widget : this.screen.settingsFields) {
            widget.setFocused(widget == clickedWidget);
        }
        this.focusedWidget = clickedWidget;

        return anyClicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.mouseReleased(absoluteX, absoluteY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.mouseDragged(absoluteX, absoluteY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.isFocused() && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (AbstractWidget widget : this.screen.settingsFields) {
            if (widget.isFocused() && widget.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean isFocused() {
        return this.focusedWidget != null && this.focusedWidget.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            for (AbstractWidget widget : this.screen.settingsFields) {
                widget.setFocused(false);
            }
        } else if (this.focusedWidget != null) {
            this.focusedWidget.setFocused(true);
        }
    }

    @Override
    public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
    }
}
