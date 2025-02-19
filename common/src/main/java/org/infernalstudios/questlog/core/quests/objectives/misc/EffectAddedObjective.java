package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.infernalstudios.questlog.util.CachedValue;
import org.infernalstudios.questlog.util.JsonUtils;

public class EffectAddedObjective extends Objective {

  private final CachedValue<MobEffect> effect;

  public EffectAddedObjective(JsonObject definition) {
    super(definition);
    this.effect = new CachedValue<>(() ->
      BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(JsonUtils.getString(definition, "effect")))
    );
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onEffectAdded);
  }

  private void onEffectAdded(QLEntityEvent.EffectAdded event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player)) {
      if (event.effect.getEffect().equals(this.effect.get())) {
        this.setUnits(this.getUnits() + 1);
      }
    }
  }
}
