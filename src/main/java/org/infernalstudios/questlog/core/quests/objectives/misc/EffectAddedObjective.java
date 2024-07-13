package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.GenericEventBus;

import javax.annotation.Nullable;

public class EffectAddedObjective extends Objective {
  private final ResourceLocation effect;
  @Nullable
  private MobEffect effectCache = null;

  public EffectAddedObjective(JsonObject definition) {
    super(definition);
    this.effect = new ResourceLocation(definition.get("effect").getAsString());
  }

  private MobEffect getEffect() {
    if (this.effectCache == null) {
      this.effectCache = ForgeRegistries.MOB_EFFECTS.getValue(this.effect);
    }
    return this.effectCache;
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onEffectAdded);
  }

  private void onEffectAdded(MobEffectEvent.Added event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.getEntity() instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player)
    ) {
      if (event.getEffectInstance().getEffect().equals(this.getEffect())) {
        this.setUnits(this.getUnits() + 1);
      }
    }
  }
}
