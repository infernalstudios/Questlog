package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
//? if >=1.21 {
import net.minecraft.advancements.AdvancementHolder;
//?} else
//import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

public class AdvancementObjective extends Objective {

    private final ResourceLocation advancement;
    private int ticksUntilCheck = 0;

    public AdvancementObjective(JsonObject definition) {
        super(definition);
        this.advancement = Questlog.parseId(JsonUtils.getString(definition, "advancement"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        this.addListener(TriggerPlayerEvent.Tick.class, this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;

        // Check once per second (20 ticks) to maintain performance
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            //? if >=1.21 {
            AdvancementHolder advancementNode = player.getServer().getAdvancements().get(this.advancement);
            //?} else
            //Advancement advancementNode = player.getServer().getAdvancements().getAdvancement(this.advancement);

            if (advancementNode != null && player.getAdvancements().getOrStartProgress(advancementNode).isDone()) {
                this.setUnits(this.getUnits() + 1);
            }

            ticksUntilCheck = 20;
        }
    }
}