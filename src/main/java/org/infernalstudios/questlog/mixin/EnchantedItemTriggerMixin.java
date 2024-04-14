package org.infernalstudios.questlog.mixin;

import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.infernalstudios.questlog.event.ItemEnchantedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemTrigger.class)
public class EnchantedItemTriggerMixin {
  @Inject(
      method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V",
      at = @At("HEAD")
  )
  public void onEnchanted(ServerPlayer player, ItemStack stack, int enchantSlot, CallbackInfo ci) {
    MinecraftForge.EVENT_BUS.post(new ItemEnchantedEvent(player, stack, enchantSlot));
  }
}
