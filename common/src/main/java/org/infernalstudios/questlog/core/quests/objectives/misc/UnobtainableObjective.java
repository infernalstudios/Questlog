package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

public class UnobtainableObjective extends Objective {

    public UnobtainableObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}