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
        if (screen.entryNameBox != null) height += 28;
        if (screen.entryTargetBox != null) height += 28;
        if (screen.entrySlotBox != null) height += 28;
        if (screen.entryNbtBox != null) height += 28;
        if (screen.entryAmountBox != null) height += 28;
        if (screen.entryIconBox != null) height += 28;
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
        if (screen.entryTargetBox != null) {
            rows.add(new EntryRow(Component.literal(screen.getTargetFieldLabel()), screen.entryTargetBox));
        }
        if (screen.entrySlotBox != null) {
            rows.add(new EntryRow(Component.literal("Slot (Optional):"), screen.entrySlotBox));
        }
        if (screen.entryNbtBox != null) {
            String labelStr = screen.isEntityObjective(screen.editingType) ? "Custom Name/Predicate (Optional):" : "NBT (Optional):";
            rows.add(new EntryRow(Component.literal(labelStr), screen.entryNbtBox));
        }
        if (screen.entryAmountBox != null) {
            String amtLabel = screen.activeTab == QuestEditorScreen.ActiveTab.REWARDS ?
                    (("questlog:choice".equals(screen.editingType) || "choice".equals(screen.editingType)) ? "Pick Count:" : "Count/Exp:")
                    : "Req Amount:";
            rows.add(new EntryRow(Component.literal(amtLabel), screen.entryAmountBox));
        }
        if (screen.entryIconBox != null) {
            rows.add(new EntryRow(Component.literal("Icon (Optional):"), screen.entryIconBox));
        }
        return rows;
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
            ps.drawString(screen.getFont(), row.label, startX, y, color, false);
            row.widget.setX(startX);
            row.widget.setY(y + 10);
            row.widget.render(ps, renderMouseX, renderMouseY, partialTicks);
            currentY += 28;
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
        for (EntryRow row : getRows()) {
            if (row.widget.mouseClicked(absoluteX, absoluteY, button)) {
                anyClicked = true;
                clickedWidget = row.widget;
            }
        }

        for (EntryRow row : getRows()) {
            row.widget.setFocused(row.widget == clickedWidget);
        }
        this.focusedWidget = clickedWidget;

        return anyClicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (EntryRow row : getRows()) {
            if (row.widget.mouseReleased(absoluteX, absoluteY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (EntryRow row : getRows()) {
            if (row.widget.mouseDragged(absoluteX, absoluteY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (EntryRow row : getRows()) {
            if (row.widget.isFocused() && row.widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (EntryRow row : getRows()) {
            if (row.widget.isFocused() && row.widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (EntryRow row : getRows()) {
            if (row.widget.isFocused() && row.widget.charTyped(codePoint, modifiers)) return true;
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
            for (EntryRow row : getRows()) {
                row.widget.setFocused(false);
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

    private record EntryRow(Component label, AbstractWidget widget) {
    }
}
