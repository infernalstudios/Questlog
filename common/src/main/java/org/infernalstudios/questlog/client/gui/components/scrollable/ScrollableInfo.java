package org.infernalstudios.questlog.client.gui.components.scrollable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.components.InfoEntry;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

public class ScrollableInfo implements Scrollable, GuiEventListener {
    private static final int REWARD_HEADING_HEIGHT = 24;
    private final QuestDetails questDetails;
    private final QuestDisplayData display;
    private List<InfoEntry> rewards;
    private List<InfoEntry> objectives;
    private List<InfoEntry> entries;
    @Nullable
    private ScrollableComponent parent = null;

    public ScrollableInfo(QuestDetails questDetails, QuestDisplayData display) {
        this.questDetails = questDetails;
        this.display = display;
    }

    private List<InfoEntry> getEntries() {
        if (this.entries != null) return this.entries;
        if (this.objectives == null) {
            this.objectives = new ArrayList<>();
            for (ObjectiveDisplayData datum : this.display.getObjectiveDisplayData()) {
                this.objectives.add(new InfoEntry(this.questDetails, datum, 0, 0));
            }
        }
        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
            for (RewardDisplayData datum : this.display.getRewardDisplayData()) {
                this.rewards.add(new InfoEntry(this.questDetails, datum, 0, 0, display));
            }
        }
        this.entries = new ArrayList<>(this.objectives);
        this.entries.addAll(this.rewards);
        return this.entries;
    }

    private boolean hasRewardHeading() {
        return !this.objectives.isEmpty() && !this.rewards.isEmpty();
    }

    private int getRewardStart() {
        return this.objectives.size() * InfoEntry.INFO_ENTRY_HEIGHT
                + (this.hasRewardHeading() ? REWARD_HEADING_HEIGHT : 0);
    }

    private int getEntryY(int index) {
        return InfoEntry.INFO_ENTRY_HEIGHT * index
                + (index >= this.objectives.size() && this.hasRewardHeading() ? REWARD_HEADING_HEIGHT : 0);
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
                    + this.objectives.size() * InfoEntry.INFO_ENTRY_HEIGHT;
            int width = (this.parent != null ? this.parent.width : this.display.getRightPanelWidth() - 36) - 15;
            ps.fill(x, y + 4, x + width, y + 5, 0xFF000000 | this.questDetails.getPalette().progressTextColor());
            ps.drawString(Minecraft.getInstance().font, Component.translatable("questlog.info.rewards"),
                    x, y + 10, this.questDetails.getPalette().titleColor(), false);
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
        this.getEntries();
        double rewardY = mouseY - this.getRewardStart();
        if (rewardY < 0) return false;
        int index = (int) (rewardY / InfoEntry.INFO_ENTRY_HEIGHT);
        return index < this.rewards.size() && this.rewards.get(index).handleChoiceClick();
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
    }
}
