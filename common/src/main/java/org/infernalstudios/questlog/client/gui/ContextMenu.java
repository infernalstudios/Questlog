package org.infernalstudios.questlog.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ContextMenu {
    private static final int MAX_VISIBLE_ITEMS = 8;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<ContextMenuItem> items;
    private int scrollOffset = 0;

    public ContextMenu(int x, int y, List<ContextMenuItem> items, Font font, int screenWidth, int screenHeight) {
        this.items = items;
        int maxW = 0;
        for (ContextMenuItem item : items) {
            maxW = Math.max(maxW, font.width(item.label()));
        }

        int extraW = items.size() > MAX_VISIBLE_ITEMS ? 26 : 20;
        this.width = maxW + extraW;

        int visibleCount = Math.min(items.size(), MAX_VISIBLE_ITEMS);
        this.height = visibleCount * 18 + 6;

        if (x + this.width > screenWidth) {
            this.x = screenWidth - this.width - 2;
        } else {
            this.x = x;
        }
        if (y + this.height > screenHeight) {
            this.y = screenHeight - this.height - 2;
        } else {
            this.y = y;
        }
    }

    public boolean mouseScrolled(double scrollY) {
        if (this.items.size() > MAX_VISIBLE_ITEMS) {
            int maxScroll = this.items.size() - MAX_VISIBLE_ITEMS;
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset - (int) Math.round(scrollY), maxScroll));
            return true;
        }
        return false;
    }

    public void render(GuiGraphics ps, int mouseX, int mouseY, Font font) {
        ps.pose().pushPose();
        ps.pose().translate(0, 0, 400);

        ps.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF181818);
        ps.fill(this.x - 1, this.y, this.x, this.y + this.height, 0xFF505050);
        ps.fill(this.x + this.width, this.y, this.x + this.width + 1, this.y + this.height, 0xFF505050);
        ps.fill(this.x, this.y - 1, this.x + this.width, this.y, 0xFF505050);
        ps.fill(this.x, this.y + this.height, this.x + this.width, this.y + this.height + 1, 0xFF505050);

        int visibleCount = Math.min(this.items.size(), MAX_VISIBLE_ITEMS);
        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = i + this.scrollOffset;
            if (itemIndex >= this.items.size()) break;

            ContextMenuItem item = this.items.get(itemIndex);
            int itemY = this.y + 3 + i * 18;

            int itemWidth = this.width - (this.items.size() > MAX_VISIBLE_ITEMS ? 7 : 0);
            boolean hovered = mouseX >= this.x && mouseX <= this.x + itemWidth && mouseY >= itemY && mouseY <= itemY + 18;
            if (hovered) {
                ps.fill(this.x + 2, itemY, this.x + itemWidth - 2, itemY + 18, 0xFF404040);
            }
            ps.drawString(font, item.label(), this.x + 10, itemY + 5, hovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
        }

        if (this.items.size() > MAX_VISIBLE_ITEMS) {
            int scrollbarWidth = 4;
            int scrollbarX = this.x + this.width - scrollbarWidth - 2;
            int scrollbarY = this.y + 3;
            int scrollbarHeight = this.height - 6;

            ps.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFF202020);

            int maxScroll = this.items.size() - MAX_VISIBLE_ITEMS;
            int thumbHeight = Math.max(8, (MAX_VISIBLE_ITEMS * scrollbarHeight) / this.items.size());
            int thumbY = scrollbarY + (this.scrollOffset * (scrollbarHeight - thumbHeight)) / maxScroll;
            ps.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF808080);
        }

        ps.pose().popPose();

        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = i + this.scrollOffset;
            if (itemIndex >= this.items.size()) break;

            ContextMenuItem item = this.items.get(itemIndex);
            if (item.tooltip() != null) {
                int itemY = this.y + 3 + i * 18;
                int itemWidth = this.width - (this.items.size() > MAX_VISIBLE_ITEMS ? 7 : 0);
                boolean hovered = mouseX >= this.x && mouseX <= this.x + itemWidth && mouseY >= itemY && mouseY <= itemY + 18;
                if (hovered) {
                    ps.renderTooltip(font, item.tooltip(), mouseX, mouseY);
                    break;
                }
            }
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            int visibleCount = Math.min(this.items.size(), MAX_VISIBLE_ITEMS);
            for (int i = 0; i < visibleCount; i++) {
                int itemIndex = i + this.scrollOffset;
                if (itemIndex >= this.items.size()) break;

                int itemY = this.y + 3 + i * 18;
                int itemWidth = this.width - (this.items.size() > MAX_VISIBLE_ITEMS ? 7 : 0);
                if (mouseX >= this.x && mouseX <= this.x + itemWidth && mouseY >= itemY && mouseY <= itemY + 18) {
                    this.items.get(itemIndex).action().run();
                    break;
                }
            }
        }
    }
}
