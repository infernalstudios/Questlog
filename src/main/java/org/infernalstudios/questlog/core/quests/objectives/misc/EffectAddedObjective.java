package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

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
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onPlayerMove);
  }

  private void onPlayerMove(MobEffectEvent.Added event) {
    if (this.isCompleted()) return;
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
