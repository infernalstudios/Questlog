package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.infernalstudios.questlog.QuestlogEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin {
  @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/io/File;Ljava/io/File;Ljava/io/File;)V"))
  private void savePlayerData(Player player, CallbackInfo info) {
    if (player instanceof ServerPlayer serverPlayer) {
      QuestlogEvents.onPlayerSave(serverPlayer);
    }
  }
}
