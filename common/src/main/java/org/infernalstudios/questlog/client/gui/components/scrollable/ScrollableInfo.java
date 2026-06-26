package org.infernalstudios.questlog.client.gui.components.scrollable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
    private final QuestDetails questDetails;
    private final QuestDisplayData display;
    private List<InfoEntry> rewards;
    private List<InfoEntry> objectives;
    @Nullable
    private ScrollableComponent parent = null;

    public ScrollableInfo(QuestDetails questDetails, QuestDisplayData display) {
        this.questDetails = questDetails;
        this.display = display;
    }

    private List<InfoEntry> getEntries() {
        if (questDetails.quest.isCompleted()) {
            if (this.rewards == null) {
                this.rewards = new ArrayList<>();
                List<RewardDisplayData> data = questDetails.getDisplay().getRewardDisplayData();
                for (RewardDisplayData datum : data) {
                    this.rewards.add(new InfoEntry(this.questDetails, datum, 0, 0, display));
                }
            }
            return this.rewards;
        } else {
            if (this.objectives == null) {
                this.objectives = new ArrayList<>();
                List<ObjectiveDisplayData> data = questDetails.getDisplay().getObjectiveDisplayData();
                for (ObjectiveDisplayData datum : data) {
                    this.objectives.add(new InfoEntry(this.questDetails, datum, 0, 0));
                }
            }
            return this.objectives;
        }
    }

    @Override
    public int getHeight() {
        return this.getEntries().size() * InfoEntry.INFO_ENTRY_HEIGHT;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        List<InfoEntry> entries = this.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            InfoEntry entry = entries.get(i);
            entry.x = this.parent != null ? (int) this.parent.getXOffset() : 0;
            entry.y = this.parent != null ? (int) this.parent.getYOffset() + InfoEntry.INFO_ENTRY_HEIGHT * i : 0;

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
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;
        if (!questDetails.quest.isCompleted()) return false;

        List<InfoEntry> entries = this.getEntries();
        int index = (int) (mouseY / InfoEntry.INFO_ENTRY_HEIGHT);
        if (index >= 0 && index < entries.size()) {
            return entries.get(index).handleChoiceClick();
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