package org.infernalstudios.questlog.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

public interface IPlatformHelper {
    <T> void sendPacketToClient(ServerPlayer player, T packet);
    <T> void sendPacketToServer(T packet);
}