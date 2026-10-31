package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitStructureObjective extends Objective {

    private final ResourceKey<Structure> structureKey;
    private final TagKey<Structure> structureTag;
    private int ticksUntilCheck = 0;

    public VisitStructureObjective(JsonObject definition) {
        super(definition);
        String structure = JsonUtils.getString(definition, "structure");
        if (structure.startsWith("#")) {
            this.structureKey = null;
            this.structureTag = TagKey.create(Registries.STRUCTURE, Questlog.parseId(structure.substring(1)));
        } else {
            this.structureKey = ResourceKey.create(Registries.STRUCTURE, Questlog.parseId(structure));
            this.structureTag = null;
        }
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        this.addListener(TriggerPlayerEvent.Tick.class, this::onPlayerMove);
    }

    private void onPlayerMove(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            ticksUntilCheck = 20;
            if (!player.serverLevel().isLoaded(player.blockPosition())) return;

            StructureManager structureManager = player.serverLevel().structureManager();
            boolean inside;
            if (this.structureTag != null) {
                inside = structureManager.getStructureWithPieceAt(player.blockPosition(), this.structureTag).isValid();
            } else {
                Structure structure = player.serverLevel().registryAccess()
                        .registryOrThrow(Registries.STRUCTURE)
                        .get(this.structureKey);
                inside = structure != null && structureManager.getStructureWithPieceAt(player.blockPosition(), structure).isValid();
            }

            if (inside) this.setUnits(this.getUnits() + 1);
        }
    }
}
