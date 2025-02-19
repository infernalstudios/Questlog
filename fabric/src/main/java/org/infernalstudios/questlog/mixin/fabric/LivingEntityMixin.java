package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
  @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("RETURN"))
  private void onAddEffect(MobEffectInstance mobEffectInstance, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValueZ()) {
      Questlog.EVENTS.post(new QLEntityEvent.EffectAdded((LivingEntity) (Object) this, mobEffectInstance));
    }
  }
}
