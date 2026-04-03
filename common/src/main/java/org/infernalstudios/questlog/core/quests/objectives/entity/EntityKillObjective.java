package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerEntityEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

public class EntityKillObjective extends AbstractEntityObjective {

    public EntityKillObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onEntityDeath);
    }

    private void onEntityDeath(TriggerEntityEvent.Death event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.damageSource.getEntity() instanceof ServerPlayer player &&
                        this.getParent().manager.player.equals(player) &&
                        this.test(event.entity)
        ) {
            this.setUnits(this.getUnits() + 1);
        }
    }
}
