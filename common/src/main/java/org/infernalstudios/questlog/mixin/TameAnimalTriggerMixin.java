package org.infernalstudios.questlog.mixin;

import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameAnimalTrigger.class)
public class TameAnimalTriggerMixin {
    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/animal/Animal;)V", at = @At("HEAD"))
    public void onTamed(ServerPlayer player, Animal animal, CallbackInfo ci) {
        Questlog.EVENTS.post(new QLEntityEvent.TameAnimal(player, animal));
    }
}
