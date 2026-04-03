package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class AdvancementObjective extends Objective {

    private final ResourceLocation advancement;
    private int ticksUntilCheck = 0;

    public AdvancementObjective(JsonObject definition) {
        super(definition);
        this.advancement = ResourceLocation.parse(JsonUtils.getString(definition, "advancement"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;

        // Check once per second (20 ticks) to maintain performance
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            AdvancementHolder advancementNode = player.getServer().getAdvancements().get(this.advancement);

            if (advancementNode != null && player.getAdvancements().getOrStartProgress(advancementNode).isDone()) {
                this.setUnits(this.getUnits() + 1);
            }

            ticksUntilCheck = 20;
        }
    }
}