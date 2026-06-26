package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.compat.origins.OriginsHelper;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class OriginObjective extends Objective {

    private final ResourceLocation origin;
    private int ticksUntilCheck = 0;

    public OriginObjective(JsonObject definition) {
        super(definition);
        this.origin = new ResourceLocation(JsonUtils.getString(definition, "origin"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;

        if (event.player instanceof ServerPlayer player
                && this.getParent().manager.player.equals(player)
                && --ticksUntilCheck <= 0) {

            if (OriginsHelper.hasOrigin(player, this.origin)) {
                this.setUnits(this.getRequiredAmount());
            }

            ticksUntilCheck = 100;
        }
    }
}
