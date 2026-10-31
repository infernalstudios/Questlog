package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitBiomeObjective extends Objective {

    private final ResourceLocation biome;
    private final TagKey<Biome> biomeTag;
    // Checks every second for performance
    private int ticksUntilCheck = 0;

    public VisitBiomeObjective(JsonObject definition) {
        super(definition);
        String biome = JsonUtils.getString(definition, "biome");
        if (biome.startsWith("#")) {
            this.biome = null;
            this.biomeTag = TagKey.create(Registries.BIOME, Questlog.parseId(biome.substring(1)));
        } else {
            this.biome = Questlog.parseId(biome);
            this.biomeTag = null;
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
            Holder<Biome> current = player.serverLevel().getBiome(player.blockPosition());
            if (this.biomeTag != null ? current.is(this.biomeTag) : current.is(this.biome)) {
                this.setUnits(this.getUnits() + 1);
            }
            ticksUntilCheck = 20;
        }
    }
}
