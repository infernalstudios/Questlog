package org.infernalstudios.questlog.core.quests.objectives.item;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

public class ItemCraftObjective extends AbstractItemObjective {

    public ItemCraftObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onItemCraft);
    }

    private void onItemCraft(TriggerPlayerEvent.Craft event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && this.test(event.outputItem)
        ) {
            this.setUnits(this.getUnits() + event.outputItem.getCount());
        }
    }
}
