package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class FarmBlockMixin {
  @Inject(
      method = "fallOn",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/level/block/FarmBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
          shift = At.Shift.AFTER
      )
  )
  private void onFallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f, CallbackInfo ci) {
    if (entity instanceof LivingEntity livingEntity) {
      Questlog.EVENTS.post(new QLBlockEvent.FarmlandTrample(blockState, blockPos, livingEntity));
    }
  }
}
