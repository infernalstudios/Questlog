package org.infernalstudios.questlog.client.gui.components.scrollable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.components.InfoEntry;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScrollableInfo implements Scrollable, GuiEventListener {
    private static final int REWARD_HEADING_HEIGHT = 34;
    private static final int REWARD_HEADING_TITLE_Y = 8;
    private static final int TITLE_HEIGHT = 16;
    private static final int HR_Y_OFFSET = -2;
    private final QuestDetails questDetails;
    private final QuestDisplayData display;
    private final boolean showRewardPreviews;
    private List<InfoEntry> entries;
    private boolean entriesCompleted;
    @Nullable
    private ScrollableComponent parent = null;

    public ScrollableInfo(QuestDetails questDetails, QuestDisplayData display, boolean showRewardPreviews) {
        this.questDetails = questDetails;
        this.display = display;
        this.showRewardPreviews = showRewardPreviews;
    }

    private boolean isPreviewing() {
        return this.showRewardPreviews && !this.questDetails.quest.isCompleted();
    }

    private List<InfoEntry> getEntries() {
        boolean completed = this.questDetails.quest.isCompleted();
        if (this.entries == null || this.entriesCompleted != completed) {
            this.entries = new ArrayList<>();
            if (!completed) {
                for (ObjectiveDisplayData datum : this.display.getObjectiveDisplayData()) {
                    this.entries.add(new InfoEntry(this.questDetails, datum, 0, 0));
                }
            }
            if (completed || this.showRewardPreviews) {
                for (RewardDisplayData datum : this.display.getRewardDisplayData()) {
                    this.entries.add(new InfoEntry(this.questDetails, datum, 0, 0, display));
                }
            }
            this.entriesCompleted = completed;
        }
        return this.entries;
    }

    private boolean hasRewardHeading() {
        return this.isPreviewing() && !this.display.getObjectiveDisplayData().isEmpty()
                && !this.display.getRewardDisplayData().isEmpty();
    }

    private int getEntryY(int index) {
        return InfoEntry.INFO_ENTRY_HEIGHT * index
                + (index >= this.display.getObjectiveDisplayData().size() && this.hasRewardHeading() ? REWARD_HEADING_HEIGHT : 0);
    }

    @Override
    public int getHeight() {
        return this.getEntries().size() * InfoEntry.INFO_ENTRY_HEIGHT
                + (this.hasRewardHeading() ? REWARD_HEADING_HEIGHT : 0);
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        List<InfoEntry> entries = this.getEntries();
        if (this.hasRewardHeading()) {
            int x = this.parent != null ? (int) this.parent.getXOffset() : 0;
            int y = (this.parent != null ? (int) this.parent.getYOffset() : 0)
                    + this.display.getObjectiveDisplayData().size() * InfoEntry.INFO_ENTRY_HEIGHT;
            int width = this.parent != null ? this.parent.width : this.display.getRightPanelWidth() - 36;
            Font font = Minecraft.getInstance().font;
            Component title = Component.translatable("questlog.info.rewards");
            int titleX = x + (width - font.width(title)) / 2;
            int titleY = y + REWARD_HEADING_TITLE_Y + (TITLE_HEIGHT - font.lineHeight + 2) / 2;
            int hrX = x + (width - this.questDetails.getGuiSet().panelHR.width()) / 2;
            this.questDetails.getGuiSet().panelHR.blit(ps, hrX, y);
            ps.drawString(font, title, titleX, titleY, this.questDetails.getPalette().titleColor(), false);
            this.questDetails.getGuiSet().panelHR.blit(ps,
                    hrX, y + REWARD_HEADING_TITLE_Y + TITLE_HEIGHT + HR_Y_OFFSET);
        }
        for (int i = 0; i < entries.size(); i++) {
            InfoEntry entry = entries.get(i);
            entry.x = this.parent != null ? (int) this.parent.getXOffset() : 0;
            entry.y = (this.parent != null ? (int) this.parent.getYOffset() : 0) + this.getEntryY(i);

            int absMouseX = this.parent != null ? mouseX + (int) this.parent.getXOffset() : mouseX;
            int absMouseY = this.parent != null ? mouseY + (int) this.parent.getYOffset() : mouseY;

            entry.render(ps, absMouseX, absMouseY, partialTicks);
        }
    }

    @Override
    public void setScrollableComponent(ScrollableComponent parent) {
        this.parent = parent;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !questDetails.quest.isCompleted()
                || questDetails.quest.isRewarded()) return false;
        List<InfoEntry> entries = this.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            double entryY = InfoEntry.INFO_ENTRY_HEIGHT * i;
            if (mouseY >= entryY && mouseY < entryY + InfoEntry.INFO_ENTRY_HEIGHT) {
                if (entries.get(i).handleChoiceClick()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
    }
}
