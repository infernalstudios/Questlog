package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QuestEvent;

public class ReadObjective extends Objective {
    public ReadObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Questlog.EVENTS.addListener(this::onQuestRead);
    }

    private void onQuestRead(QuestEvent.Read event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player.equals(this.getParent().manager.player) && event.quest.getId().equals(this.getParent().getId())) {
            this.setUnits(this.getUnits() + 1);
        }
    }

    @Override
    public boolean isReadObjective() {
        return true;
    }
}