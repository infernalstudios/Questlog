package org.infernalstudios.questlog.mixin;

import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemUsedOnLocationTrigger.class)
public class ItemUsedOnLocationTriggerMixin {
    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    public void onBlockInteract(ServerPlayer player, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        BlockState state = player.level().getBlockState(pos);
        Questlog.EVENTS.post(new QLBlockEvent.Interact(state, pos, player, stack));
    }
}