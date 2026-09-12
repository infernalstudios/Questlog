package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerEntityEvent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.CachedValue;
import org.infernalstudios.questlog.util.JsonUtils;

public class EffectAddedObjective extends Objective {

    private final CachedValue<MobEffect> effect;
    private final ResourceLocation effectId;

    public EffectAddedObjective(JsonObject definition) {
        super(definition);
        String effectStr = JsonUtils.getString(definition, "effect");
        this.effectId = effectStr.contains(":") ? ResourceLocation.tryParse(effectStr) : ResourceLocation.fromNamespaceAndPath("minecraft", effectStr);
        this.effect = new CachedValue<>(() ->
                this.effectId != null ? BuiltInRegistries.MOB_EFFECT.get(this.effectId) : null
        );
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onEffectAdded);
    }

    private void onEffectAdded(TriggerEntityEvent.EffectAdded event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player)) {
            MobEffect eventEffect = event.effect.getEffect().value();
            if ((this.effect.get() != null && eventEffect.equals(this.effect.get())) ||
                    (this.effectId != null && this.effectId.equals(BuiltInRegistries.MOB_EFFECT.getKey(eventEffect)))) {
                this.setUnits(this.getUnits() + 1);
            }
        }
    }
}
