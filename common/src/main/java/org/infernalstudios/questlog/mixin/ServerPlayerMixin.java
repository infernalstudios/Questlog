package org.infernalstudios.questlog.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
  @Inject(method = "awardStat", at = @At("HEAD"))
  private void onAwardStat(Stat<?> stat, int amount, CallbackInfo ci) {
    Questlog.EVENTS.post(new QLPlayerEvent.StatAward((ServerPlayer) (Object) this, stat, amount));
  }
}
