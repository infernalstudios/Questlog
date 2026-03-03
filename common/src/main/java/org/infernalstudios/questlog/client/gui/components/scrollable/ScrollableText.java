package org.infernalstudios.questlog.client.gui.components.scrollable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent.Scrollable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ScrollableText implements Scrollable, GuiEventListener {

    private final Font font;
    private final FormattedText text;
    private final int color;

    @Nullable
    private List<FormattedCharSequence> lines;

    private ScrollableComponent scroller;

    public ScrollableText(Font font, FormattedText text, int color) {
        this.font = font;
        this.text = text;
        this.color = color;
    }

    private int getWidth() {
        return this.scroller.width - this.scroller.getScrollbarWidth();
    }

    private List<FormattedCharSequence> getLines() {
        if (this.lines == null) {
            this.lines = this.font.split(this.text, this.getWidth());
        }

        return lines;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.getLines().size(); i++) {
            ps.drawString(
                    this.font,
                    this.getLines().get(i),
                    (int) this.scroller.getXOffset(),
                    (int) this.scroller.getYOffset() + i * this.font.lineHeight,
                    this.color,
                    false
            );
        }
    }

    @Nullable
    public Style getStyleAt(double mouseX, double mouseY) {
        if (this.getLines() == null || this.scroller == null) return null;

        int lineIndex = (int) mouseY / this.font.lineHeight;

        if (lineIndex >= 0 && lineIndex < this.getLines().size()) {
            return this.font.getSplitter().componentStyleAtWidth(this.getLines().get(lineIndex), (int) mouseX);
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style style = this.getStyleAt(mouseX, mouseY);
        return style != null && style.getClickEvent() != null;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean b) {
    }

    @Override
    public int getHeight() {
        return this.getLines().size() * this.font.lineHeight;
    }

    @Override
    public void setScrollableComponent(ScrollableComponent component) {
        this.scroller = component;
    }
}
