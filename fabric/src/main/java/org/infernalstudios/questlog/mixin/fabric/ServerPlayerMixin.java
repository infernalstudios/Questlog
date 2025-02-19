package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
  @Inject(
      method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
      at = @At(value = "RETURN", ordinal = 1)
  )
  private void onDrop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
     Questlog.EVENTS.post(new QLEntityEvent.TossItem((ServerPlayer) (Object) this, itemStack));
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void onTick(CallbackInfo ci) {
    Questlog.EVENTS.post(new QLPlayerEvent.Tick((ServerPlayer) (Object) this));
  }
}
