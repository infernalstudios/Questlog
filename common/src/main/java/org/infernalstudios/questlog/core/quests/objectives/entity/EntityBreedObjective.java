package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class EntityBreedObjective extends AbstractEntityObjective {

    public EntityBreedObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners(QuestlogEventBus bus) {
        super.registerEventListeners(bus);
        bus.addListener(this::onEntityBreed);
    }

    private void onEntityBreed(QLEntityEvent.Breed event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.causedByPlayer instanceof ServerPlayer player &&
                this.getParent().manager.player.equals(player) &&
                (this.test(event.parentA) || this.test(event.parentB))
        ) {
            this.setUnits(this.getUnits() + 1);
        }
    }
}
