package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitStructureObjective extends Objective {

    private final ResourceKey<Structure> structure;
    // Checks every second for performance
    private int ticksUntilCheck = 0;

    public VisitStructureObjective(JsonObject definition) {
        super(definition);
        this.structure = ResourceKey.create(
                Registries.STRUCTURE,
                new ResourceLocation(JsonUtils.getString(definition, "structure"))
        );
    }

    @Override
    public void registerEventListeners(QuestlogEventBus bus) {
        super.registerEventListeners(bus);
        bus.addListener(this::onPlayerMove);
    }

    private void onPlayerMove(QLPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            ticksUntilCheck = 20;
            if (!player.serverLevel().isLoaded(player.blockPosition())) return;
            if (!player.serverLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), this.structure).isValid())
                return;

            this.setUnits(this.getUnits() + 1);
        }
    }
}
