package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitBiomeObjective extends Objective {

    private final ResourceLocation biome;
    // Checks every second for performance
    private int ticksUntilCheck = 0;

    public VisitBiomeObjective(JsonObject definition) {
        super(definition);
        this.biome = ResourceLocation.parse(JsonUtils.getString(definition, "biome"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerMove);
    }

    private void onPlayerMove(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            if (player.serverLevel().getBiome(player.blockPosition()).unwrapKey().orElseThrow().location().equals(this.biome)) {
                this.setUnits(this.getUnits() + 1);
            }
            ticksUntilCheck = 20;
        }
    }
}
