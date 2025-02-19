package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BredAnimalsTrigger.class)
public class BredAnimalsTriggerMixin {
  @Inject(method = "trigger", at = @At("HEAD"))
  private void onBredAnimalsTrigger(ServerPlayer serverPlayer, Animal parentA, Animal parentB, AgeableMob child, CallbackInfo ci) {
    Questlog.EVENTS.post(new QLEntityEvent.Breed(child, parentA, parentB, serverPlayer));
  }
}
