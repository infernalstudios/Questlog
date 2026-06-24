package org.infernalstudios.questlog.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Collection;

public interface IPlatformHelper {
    <T> void sendPacketToClient(ServerPlayer player, T packet);

    <T> void sendPacketToServer(T packet);

    Path getConfigDirectory();

    /**
     * Returns {@code true} when {@code player} has the given origin assigned on any layer.
     * Returns {@code false} if Origins is not installed.
     */
    boolean hasOrigin(ServerPlayer player, ResourceLocation originId);

    /**
     * Returns all registered origin {@link ResourceLocation} IDs available to the client,
     * or an empty collection if Origins is not installed.
     */
    Collection<ResourceLocation> getOriginIds();
}