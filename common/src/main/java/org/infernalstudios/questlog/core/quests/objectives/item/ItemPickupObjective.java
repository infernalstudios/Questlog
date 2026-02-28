package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import org.infernalstudios.questlog.Questlog;

public class ItemPickupObjective extends ItemObtainObjective {

    public ItemPickupObjective(JsonObject definition) {
        super(definition);
        Questlog.LOGGER.warn("Objective type 'questlog:item_pickup' is deprecated. It is currently redirecting to 'questlog:item_obtain' behavior and will be removed in a future release. Please update your quests!");
    }
}