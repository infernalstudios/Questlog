package org.infernalstudios.questlog.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Collection;

public interface IPlatformHelper {
    <T> void sendPacketToClient(ServerPlayer player, T packet);

    <T> void sendPacketToServer(T packet);

    Path getConfigDirectory();

    boolean hasOrigin(ServerPlayer player, ResourceLocation originId);

    Collection<ResourceLocation> getOriginIds();
}