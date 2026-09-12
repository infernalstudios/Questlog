package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class QuestCompleteObjective extends Objective {
    private final ResourceLocation quest;

    public QuestCompleteObjective(JsonObject definition) {
        super(definition);
        this.quest = new ResourceLocation(JsonUtils.getString(definition, "quest"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Questlog.EVENTS.addListener(this::onQuestCompleted);
    }

    private void onQuestCompleted(QuestEvent.Completed event) {
        if (super.isCompleted() || this.getParent() == null) return;
        if (event.player.equals(this.getParent().manager.player) && event.quest.getId().equals(this.quest)) {
            this.setUnits(this.getRequiredAmount());
        }
    }

    @Override
    public int getUnits() {
        int baseUnits = super.getUnits();
        if (baseUnits >= this.getRequiredAmount()) {
            return baseUnits;
        }

        if (this.getParent() != null && this.getParent().manager != null) {
            Quest targetQuest = this.getParent().manager.getQuest(this.quest);
            if (targetQuest != null && targetQuest.isCompleted()) {
                return this.getRequiredAmount();
            }
        }

        return baseUnits;
    }

    @Override
    public boolean isCompleted() {
        if (super.isCompleted()) {
            return true;
        }

        if (this.getParent() != null && this.getParent().manager != null) {
            Quest targetQuest = this.getParent().manager.getQuest(this.quest);
            return targetQuest != null && targetQuest.isCompleted();
        }

        return false;
    }
}