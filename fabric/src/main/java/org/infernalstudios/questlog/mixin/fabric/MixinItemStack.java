package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {
  @Inject(method = "onCraftedBy", at = @At("TAIL"))
  private void onCraftedBy(Level level, Player player, int i, CallbackInfo ci) {
    Questlog.EVENTS.post(new QLPlayerEvent.Craft(player, (ItemStack) (Object) this));
  }
}
