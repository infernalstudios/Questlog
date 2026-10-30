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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EditEntryPanelScrollable implements Scrollable, GuiEventListener, NarratableEntry {
    private final QuestEditorScreen screen;
    private ScrollableComponent scroller;
    private AbstractWidget focusedWidget = null;

    public EditEntryPanelScrollable(QuestEditorScreen screen) {
        this.screen = screen;
    }

    @Override
    public int getHeight() {
        int height = 0;
        for (EntryRow row : getRows()) height += row.height();
        return height + 8;
    }

    @Override
    public void setScrollableComponent(ScrollableComponent component) {
        this.scroller = component;
    }

    private List<EntryRow> getRows() {
        List<EntryRow> rows = new ArrayList<>();
        if (screen.entryNameBox != null) {
            rows.add(new EntryRow(Component.literal("Name (Optional):"), screen.entryNameBox));
        }
        if (screen.entryNestedButton != null) {
            rows.add(new EntryRow(null, screen.entryNestedButton));
        }
        if (screen.entryTargetBox != null) {
            rows.add(new EntryRow(Component.literal(screen.getTargetFieldLabel()), screen.entryTargetBox));
        }
        if (screen.entrySlotBox != null) {
            rows.add(new EntryRow(Component.literal("Slot (Optional):"), screen.entrySlotBox));
        }
        if (screen.entryComponentsBox != null) {
            //? if >=1.21 {
            String labelStr = screen.isEntityObjective(screen.editingType) ? "Predicate (Optional):" : "Components (Optional):";
            //?} else
            //String labelStr = screen.isEntityObjective(screen.editingType) ? "Predicate (Optional):" : "NBT (Optional):";
            rows.add(new EntryRow(Component.literal(labelStr), screen.entryComponentsBox));
        }
        if (screen.entryAmountBox != null) {
            rows.add(new EntryRow(Component.literal(screen.getAmountFieldLabel()), screen.entryAmountBox, screen.entryLevelsButton));
        }
        if (screen.entryIconBox != null) {
            rows.add(new EntryRow(Component.literal("Icon (Optional):"), screen.entryIconBox));
        }
        return rows;
    }

    private List<AbstractWidget> getWidgets() {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (EntryRow row : getRows()) {
            widgets.add(row.widget);
            if (row.sideWidget != null) widgets.add(row.sideWidget);
        }
        return widgets;
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

        int startX = panel2X + 15;
        int startY = panel2Y + 28;
        int scroll = (int) this.scroller.getScrollAmount();

        int absMouseX = mouseX + (int) this.scroller.getXOffset();
        int absMouseY = mouseY + (int) this.scroller.getYOffset();
        boolean hovered = this.scroller.isMouseOver(absMouseX, absMouseY);
        int renderMouseX = hovered ? absMouseX : -9999;
        int renderMouseY = hovered ? absMouseY : -9999;

        int color = Questlog.getConfig().colors.textColor | 0xFF000000;

        List<EntryRow> rows = getRows();
        int currentY = 0;
        for (EntryRow row : rows) {
            int y = startY + currentY - scroll;
            if (row.label != null) {
                ps.drawString(screen.getFont(), row.label, startX, y, color, false);
                y += 10;
            }
            row.widget.setX(startX);
            row.widget.setY(y);
            row.widget.render(ps, renderMouseX, renderMouseY, partialTicks);
            if (row.sideWidget != null) {
                row.sideWidget.setX(startX + row.widget.getWidth() + 5);
                row.sideWidget.setY(y);
                row.sideWidget.render(ps, renderMouseX, renderMouseY, partialTicks);
            }
            currentY += row.height();
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
        for (AbstractWidget widget : getWidgets()) {
            if (widget.mouseClicked(absoluteX, absoluteY, button)) {
                anyClicked = true;
                clickedWidget = widget;
            }
        }

        for (AbstractWidget widget : getWidgets()) {
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

        for (AbstractWidget widget : getWidgets()) {
            if (widget.mouseReleased(absoluteX, absoluteY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (AbstractWidget widget : getWidgets()) {
            if (widget.mouseDragged(absoluteX, absoluteY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : getWidgets()) {
            if (widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : getWidgets()) {
            if (widget.isFocused() && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (AbstractWidget widget : getWidgets()) {
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
            for (AbstractWidget widget : getWidgets()) {
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

    private record EntryRow(@Nullable Component label, AbstractWidget widget, @Nullable AbstractWidget sideWidget) {
        EntryRow(@Nullable Component label, AbstractWidget widget) {
            this(label, widget, null);
        }

        int height() {
            return this.label != null ? 28 : 22;
        }
    }
}
