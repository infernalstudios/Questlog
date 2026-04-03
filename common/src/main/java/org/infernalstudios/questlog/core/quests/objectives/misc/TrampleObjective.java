package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerBlockEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

public class TrampleObjective extends Objective {

    public TrampleObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onBlockTrample);
    }

    private void onBlockTrample(TriggerBlockEvent.FarmlandTrample event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player)) {
            this.setUnits(this.getUnits() + 1);
        }
    }
}
