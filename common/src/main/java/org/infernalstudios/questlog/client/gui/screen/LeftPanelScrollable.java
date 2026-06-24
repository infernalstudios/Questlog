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

class LeftPanelScrollable implements Scrollable, GuiEventListener, NarratableEntry {
    private final QuestEditorScreen screen;
    private ScrollableComponent scroller;
    private GuiEventListener focusedBox = null;

    public LeftPanelScrollable(QuestEditorScreen screen) {
        this.screen = screen;
    }

    @Override
    public int getHeight() {
        return 200;
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

        int startX = baseX + 10;
        int startY = baseY + 12;
        int scroll = (int) this.scroller.getScrollAmount();

        screen.idBox.setX(startX + 5);
        screen.idBox.setY(startY + 10 - scroll);

        screen.titleBox.setX(startX + 5);
        screen.titleBox.setY(startY + 42 - scroll);

        screen.descriptionBox.setX(startX + 5);
        screen.descriptionBox.setY(startY + 74 - scroll);

        screen.iconBox.setX(startX + 5);
        screen.iconBox.setY(startY + 144 - scroll);

        screen.chapterBox.setX(startX + 5);
        screen.chapterBox.setY(startY + 176 - scroll);

        screen.orderBox.setX(startX + 155);
        screen.orderBox.setY(startY + 176 - scroll);

        int absMouseX = mouseX + (int) this.scroller.getXOffset();
        int absMouseY = mouseY + (int) this.scroller.getYOffset();

        boolean hovered = this.scroller.isMouseOver(absMouseX, absMouseY);
        int renderMouseX = hovered ? absMouseX : -9999;
        int renderMouseY = hovered ? absMouseY : -9999;

        int color = Questlog.getConfig().colors.textColor | 0xFF000000;
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.id"), startX + 5, startY - scroll, color, false);
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.title_label"), startX + 5, startY + 32 - scroll, color, false);
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.description_label"), startX + 5, startY + 64 - scroll, color, false);
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.icon_label"), startX + 5, startY + 134 - scroll, color, false);
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.chapter_label"), startX + 5, startY + 166 - scroll, color, false);
        ps.drawString(screen.getFont(), Component.translatable("questlog.editor.order_label"), startX + 155, startY + 166 - scroll, color, false);

        for (AbstractWidget widget : screen.leftFields) {
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
        for (AbstractWidget widget : screen.leftFields) {
            boolean canClick = (widget != screen.idBox) || (screen.questToEdit == null);
            if (canClick && widget.mouseClicked(absoluteX, absoluteY, button)) {
                anyClicked = true;
                clickedWidget = widget;
            }
        }

        for (AbstractWidget widget : screen.leftFields) {
            widget.setFocused(widget == clickedWidget);
        }
        this.focusedBox = clickedWidget;

        return anyClicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (AbstractWidget widget : screen.leftFields) {
            if (widget.mouseReleased(absoluteX, absoluteY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scroller == null) return false;
        double absoluteX = mouseX + this.scroller.getXOffset();
        double absoluteY = mouseY + this.scroller.getYOffset();

        for (AbstractWidget widget : screen.leftFields) {
            if (widget.mouseDragged(absoluteX, absoluteY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : screen.leftFields) {
            if (widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : screen.leftFields) {
            if (widget.isFocused() && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (AbstractWidget widget : screen.leftFields) {
            if (widget.isFocused() && widget.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean isFocused() {
        return this.focusedBox != null && this.focusedBox.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            for (AbstractWidget widget : screen.leftFields) {
                widget.setFocused(false);
            }
        } else {
            if (this.focusedBox != null) {
                this.focusedBox.setFocused(true);
            } else {
                AbstractWidget first = (screen.questToEdit == null) ? screen.idBox : screen.titleBox;
                first.setFocused(true);
                this.focusedBox = first;
            }
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
