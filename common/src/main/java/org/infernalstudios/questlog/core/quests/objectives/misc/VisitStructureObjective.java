package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitStructureObjective extends Objective {

    private final ResourceKey<Structure> structureKey;
    private int ticksUntilCheck = 0;

    public VisitStructureObjective(JsonObject definition) {
        super(definition);
        this.structureKey = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.parse(JsonUtils.getString(definition, "structure"))
        );
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerMove);
    }

    private void onPlayerMove(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            ticksUntilCheck = 20;
            if (!player.serverLevel().isLoaded(player.blockPosition())) return;

            Structure structure = player.serverLevel().registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .get(this.structureKey);

            if (structure == null) return;

            if (!player.serverLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), structure).isValid())
                return;

            this.setUnits(this.getUnits() + 1);
        }
    }
}