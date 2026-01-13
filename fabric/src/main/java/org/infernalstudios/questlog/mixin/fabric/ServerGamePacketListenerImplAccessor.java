package org.infernalstudios.questlog.mixin.fabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
  @Accessor("server")
  MinecraftServer getServer();
}