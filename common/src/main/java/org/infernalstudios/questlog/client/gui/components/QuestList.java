package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.scrollable.Scrollable;
import org.infernalstudios.questlog.core.quests.Quest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.screen.QuestEditorScreen;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class QuestList extends AbstractContainerEventHandler implements Scrollable {

    public final boolean condensed;
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final Consumer<Quest> onSelect;
    private final List<QuestListEntry> children = new ArrayList<>();

    @Nullable
    private QuestListEntry hovered;

    @Nullable
    private ScrollableComponent scroller;

    public QuestList(Minecraft minecraft, List<Quest> quests, Consumer<Quest> onSelect, boolean condensed) {
        this.minecraft = minecraft;
        this.condensed = condensed;
        this.itemHeight = condensed ? 18 : 28;
        this.onSelect = onSelect;

        for (Quest quest : quests) {
            this.children.add(new QuestListEntry(this, quest));
        }

        this.children.sort((a, b) -> {
            Quest qa = a.quest;
            Quest qb = b.quest;
            if (qa.isCompleted() && !qb.isCompleted()) {
                return 1;
            } else if (!qa.isCompleted() && qb.isCompleted()) {
                return -1;
            } else if (!qa.isRewarded() && qb.isRewarded()) {
                return -1;
            } else if (qa.isRewarded() && !qb.isRewarded()) {
                return 1;
            } else {
                int orderA = qa.getDisplay().getSortOrder();
                int orderB = qb.getDisplay().getSortOrder();
                if (orderA != orderB) {
                    return Integer.compare(orderA, orderB);
                }
                return qa.getDisplay().getTitle().getString().compareToIgnoreCase(qb.getDisplay().getTitle().getString());
            }
        });
    }

    public int getWidth() {
        return this.scroller != null ? this.scroller.width : 0;
    }

    @Override
    public int getHeight() {
        return this.children.size() * itemHeight;
    }

    @Override
    public void setScrollableComponent(ScrollableComponent component) {
        this.scroller = component;
    }

    private boolean isRenderingScrollbar() {
        return this.scroller != null && this.scroller.canScroll();
    }

    // Row helpers
    public int getRowWidth() {
        return this.getWidth();
    }

    public int getRowLeft() {
        return this.getWidth() / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    protected int getRowTop(int index) {
        return index * this.itemHeight;
    }

    private int getRowBottom(int index) {
        return this.getRowTop(index) + this.itemHeight;
    }

    @Nullable
    public QuestListEntry getHovered() {
        return this.hovered;
    }

    @Nullable
    public QuestListEntry getFocused() {
        return (QuestListEntry) super.getFocused();
    }

    protected QuestListEntry getEntry(int index) {
        return this.children.get(index);
    }

    protected int getItemCount() {
        return this.children.size();
    }

    @Nullable
    protected final QuestListEntry getEntryAtPosition(double x, double y) {
        int left = this.getRowLeft();
        int right = this.getRowRight() - (this.isRenderingScrollbar() ? this.scroller.getScrollbarWidth() : 0);
        if (right < x || x < left) {
            return null;
        }

        for (int i = 0; i < this.children.size(); i++) {
            int top = this.getRowTop(i);
            int bottom = this.getRowBottom(i);
            if (y >= (double) top && y <= (double) bottom) {
                return this.children.get(i);
            }
        }

        return null;
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }

    // Renderers

    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        this.renderList(ps, mouseX, mouseY, partialTicks);
    }

    protected void renderList(GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();

        for (int itemIndex = 0; itemIndex < this.getItemCount(); ++itemIndex) {
            this.renderItem(ps, mouseX, mouseY, partialTicks, itemIndex, rowLeft, this.getRowTop(itemIndex), rowWidth, this.itemHeight);
        }
    }

    protected void renderItem(
            GuiGraphics ps,
            int mouseX,
            int mouseY,
            float partialTicks,
            int itemIndex,
            int rowLeft,
            int rowTop,
            int rowWidth,
            int itemHeightAdjusted
    ) {
        QuestListEntry entry = this.getEntry(itemIndex);
        entry.render(
                ps,
                itemIndex,
                rowTop,
                rowLeft,
                rowWidth,
                itemHeightAdjusted,
                mouseX,
                mouseY,
                Objects.equals(this.hovered, entry),
                partialTicks
        );
    }

    public boolean isMouseOver(double x, double y) {
        return y >= 0 && y <= (double) this.getHeight() && x >= 0 && x <= (double) this.getWidth();
    }

    // Event handlers
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        QuestListEntry e = this.getEntryAtPosition(mouseX, mouseY);
        if (e != null) {
            return e.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public @NotNull List<QuestListEntry> children() {
        return this.children;
    }

    public static class QuestListEntry implements GuiEventListener {

        private final QuestList list;
        private final Quest quest;

        protected QuestListEntry(QuestList list, Quest quest) {
            this.list = list;
            this.quest = quest;
        }

        public Quest getQuest() {
            return this.quest;
        }

        public void render(
                GuiGraphics ps,
                int color,
                int yPosition,
                int xPosition,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean isHovered,
                float partialTicks
        ) {
            Font font = Minecraft.getInstance().font;
            int dx = 5;
            if (this.quest.getDisplay().getIcon() != null) {
                int iconY = this.list.condensed ? yPosition + (height - 16) / 2 : yPosition + 5;
                this.quest.getDisplay()
                        .getIcon()
                        .blit(ps, xPosition + dx + (int) this.list.scroller.getXOffset(), iconY + (int) this.list.scroller.getYOffset());
                dx += 20;
            }

            if (this.list.condensed) {
                int y = yPosition + (int) this.list.scroller.getYOffset() + (height - font.lineHeight) / 2 + 2;
                int currentX = xPosition + (int) this.list.scroller.getXOffset() + dx;

                Component title = this.quest.getDisplay().getTitle();
                ps.drawString(font, title, currentX, y, Questlog.getConfig().colors.textColor, false);

                if (this.quest.isFailed()) {
                    int titleWidth = font.width(title);
                    int statusX = currentX + titleWidth + 8;
                    ps.drawString(font, Component.translatable("questlog.quest.failed"), statusX, y, Questlog.getConfig().colors.failedTextColor, false);
                } else if (this.quest.isCompleted()) {
                    int titleWidth = font.width(title);
                    int statusX = currentX + titleWidth + 8;

                    Component statusText = this.quest.isRewarded()
                            ? Component.translatable("questlog.quest.completed")
                            : Component.translatable("questlog.quest.uncollected");

                    int statusColor = this.quest.isRewarded() ? Questlog.getConfig().colors.completedTextColor : Questlog.getConfig().colors.uncollectedTextColor;

                    ps.drawString(font, statusText, statusX, y, statusColor, false);
                }
            } else if (this.quest.isFailed()) {
                int linesHeight = font.lineHeight * 2;
                int dy = (height - linesHeight) / 2;

                ps.drawString(
                        font,
                        this.quest.getDisplay().getTitle(),
                        xPosition + (int) this.list.scroller.getXOffset() + dx,
                        yPosition + dy + (int) this.list.scroller.getYOffset(),
                        Questlog.getConfig().colors.textColor,
                        false
                );
                ps.drawString(
                        font,
                        Component.translatable("questlog.quest.failed"),
                        xPosition + (int) this.list.scroller.getXOffset() + dx,
                        yPosition + (int) this.list.scroller.getYOffset() + dy + font.lineHeight,
                        Questlog.getConfig().colors.failedTextColor,
                        false
                );
            } else if (this.quest.isCompleted()) {
                int linesHeight = font.lineHeight * 2;
                int dy = (height - linesHeight) / 2;

                ps.drawString(
                        font,
                        this.quest.getDisplay().getTitle(),
                        xPosition + (int) this.list.scroller.getXOffset() + dx,
                        yPosition + dy + (int) this.list.scroller.getYOffset(),
                        Questlog.getConfig().colors.textColor,
                        false
                );
                if (!this.quest.isRewarded()) {
                    ps.drawString(
                            font,
                            Component.translatable("questlog.quest.uncollected"),
                            xPosition + (int) this.list.scroller.getXOffset() + dx,
                            yPosition + (int) this.list.scroller.getYOffset() + dy + font.lineHeight,
                            Questlog.getConfig().colors.uncollectedTextColor,
                            false
                    );
                } else {
                    ps.drawString(
                            font,
                            Component.translatable("questlog.quest.completed"),
                            xPosition + (int) this.list.scroller.getXOffset() + dx,
                            yPosition + (int) this.list.scroller.getYOffset() + dy + font.lineHeight,
                            Questlog.getConfig().colors.completedTextColor,
                            false
                    );
                }
            } else {
                int y = yPosition + (int) this.list.scroller.getYOffset() + (height - font.lineHeight) / 2;
                ps.drawString(font, this.quest.getDisplay().getTitle(), xPosition + (int) this.list.scroller.getXOffset() + dx, y, Questlog.getConfig().colors.textColor, false);
            }

            if (this.hasNext()) {
                QuestlogGuiSet.DEFAULT.bigHR.blit(
                        ps,
                        (int) this.list.scroller.getXOffset() - 2,
                        yPosition + (int) this.list.scroller.getYOffset() + height - 5
                );
            }

            if (isHovered) {
                ps.fill(
                        xPosition + (int) this.list.scroller.getXOffset() - 2,
                        yPosition + (int) this.list.scroller.getYOffset(),
                        xPosition +
                                (int) this.list.scroller.getXOffset() +
                                width -
                                (this.list.isRenderingScrollbar() ? this.list.scroller.getScrollbarWidth() * 2 : 0),
                        yPosition + (int) this.list.scroller.getYOffset() + height,
                        Questlog.getConfig().colors.hoverFillColor
                );
            }

            if (QuestlogClient.isEditModeActive) {
                int gearX = xPosition + width - (this.list.isRenderingScrollbar() ? this.list.scroller.getScrollbarWidth() * 2 : 0) - 20;
                int gearY = yPosition + (height - 16) / 2;
                ps.blit(QuestEditorScreen.GEAR_ICON,
                        gearX + (int) this.list.scroller.getXOffset(),
                        gearY + (int) this.list.scroller.getYOffset(),
                        0, 0, 16, 16, 16, 16);

                if (isHovered) {
                    if (mouseX >= gearX && mouseX <= gearX + 16 && mouseY >= gearY && mouseY <= gearY + 16) {
                        ps.fill(gearX + (int) this.list.scroller.getXOffset(),
                                gearY + (int) this.list.scroller.getYOffset(),
                                gearX + 16 + (int) this.list.scroller.getXOffset(),
                                gearY + 16 + (int) this.list.scroller.getYOffset(),
                                0x40FFFFFF);
                        if (Minecraft.getInstance().screen instanceof QuestlogScreen qScreen) {
                            qScreen.pendingTooltip = Component.translatable("questlog.menu.edit");
                        }
                    }
                }
            }
        }

        private boolean hasNext() {
            return this.list.children.indexOf(this) < this.list.children.size() - 1;
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(this.list.getEntryAtPosition(mouseX, mouseY), this);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
                if (QuestlogClient.isEditModeActive) {
                    int gearX = this.list.getRowLeft() + this.list.getRowWidth() - (this.list.isRenderingScrollbar() ? this.list.scroller.getScrollbarWidth() * 2 : 0) - 20;
                    int itemIndex = this.list.children.indexOf(this);
                    int gearY = this.list.getRowTop(itemIndex) + (this.list.itemHeight - 16) / 2;
                    if (mouseX >= gearX && mouseX <= gearX + 16 && mouseY >= gearY && mouseY <= gearY + 16) {
                        Minecraft mc = Minecraft.getInstance();
                        mc.setScreen(new QuestEditorScreen(mc.screen, this.quest));
                        return true;
                    }
                }
                this.list.onSelect.accept(this.quest);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public void setFocused(boolean var1) {
        }
    }
}
