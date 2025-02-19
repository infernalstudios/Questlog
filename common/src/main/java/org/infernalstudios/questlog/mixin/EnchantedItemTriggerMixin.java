package org.infernalstudios.questlog.mixin;

import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemTrigger.class)
public class EnchantedItemTriggerMixin {
  @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"))
  public void onEnchanted(ServerPlayer player, ItemStack stack, int enchantSlot, CallbackInfo ci) {
    Questlog.EVENTS.post(new QLPlayerEvent.Enchant(player, stack, enchantSlot));
  }
}
