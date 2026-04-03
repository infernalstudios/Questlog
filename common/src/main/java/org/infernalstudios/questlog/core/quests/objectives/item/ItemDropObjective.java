package org.infernalstudios.questlog.core.quests.objectives.item;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerEntityEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

public class ItemDropObjective extends AbstractItemObjective {

    public ItemDropObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onItemDrop);
    }

    private void onItemDrop(TriggerEntityEvent.TossItem event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.entity instanceof ServerPlayer player &&
                        this.getParent().manager.player.equals(player) &&
                        this.test(event.item)
        ) {
            this.setUnits(this.getUnits() + event.item.getCount());
        }
    }
}
