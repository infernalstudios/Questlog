package org.infernalstudios.questlog.event.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QLEntityEvent extends QLEvent {
  public final LivingEntity entity;

  public QLEntityEvent(LivingEntity entity) {
    this.entity = entity;
  }

  public static class Breed extends QLEntityEvent {
    public final LivingEntity parentA;
    public final LivingEntity parentB;
    public final Player causedByPlayer;

    public Breed(LivingEntity entity, LivingEntity parentA, LivingEntity parentB, Player causedByPlayer) {
      super(entity);

      this.parentA = parentA;
      this.parentB = parentB;
      this.causedByPlayer = causedByPlayer;
    }
  }

  public static class Death extends QLEntityEvent {
    public final DamageSource damageSource;

    public Death(LivingEntity entity, DamageSource source) {
      super(entity);

      this.damageSource = source;
    }
  }

  public static class UseItem extends QLEntityEvent {
    public final ItemStack item;

    public UseItem(LivingEntity entity, ItemStack item) {
      super(entity);

      this.item = item;
    }
  }

  public static class TossItem extends QLEntityEvent {
    public final ItemStack item;

    public TossItem(LivingEntity entity, ItemStack item) {
      super(entity);

      this.item = item;
    }
  }

  public static class PickupItem extends QLEntityEvent {
    public final ItemStack item;

    public PickupItem(LivingEntity entity, ItemStack item) {
      super(entity);

      this.item = item;
    }
  }

  public static class EffectAdded extends QLEntityEvent {
    public final MobEffectInstance effect;

    public EffectAdded(LivingEntity entity, MobEffectInstance effect) {
      super(entity);

      this.effect = effect;
    }
  }
}
